package app.inspiry.export.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.net.Uri
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import app.inspiry.BuildConfig
import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.data.FRAME_IN_MILLIS
import app.inspiry.core.data.InspResponseData
import app.inspiry.core.data.OriginalTemplateData
import app.inspiry.core.database.InspDatabase
import app.inspiry.core.log.KLogger
import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.media.Template
import app.inspiry.core.notification.StoryUnfinishedNotificationManager
import app.inspiry.core.template.TemplateViewModel
import app.inspiry.export.ExportState
import app.inspiry.export.WhereToExport
import app.inspiry.export.mainui.ToGallerySaver
import app.inspiry.export.mainui.ToGallerySaverImpl
import app.inspiry.export.record.RecordListener
import app.inspiry.export.record.ThreadRecord
import app.inspiry.helpers.K
import app.inspiry.utils.*
import app.inspiry.views.group.BaseGroupZView
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.template.InspTemplateViewAndroid
import app.inspiry.views.template.RecordMode
import com.soywiz.klock.DateTime
import kotlinx.coroutines.*
import okhttp3.internal.closeQuietly
import app.inspiry.export.record.EncoderType
import kotlinx.coroutines.flow.*
import java.io.File
import java.io.OutputStream


class RecordViewModelImpl(
    saveState: Bundle?,
    initialImageElseVideo: Boolean? = null,
    val templateView: InspTemplateView,
    val innerGroupZView: BaseGroupZView,
    val activity: ComponentActivity,
    private val textureTemplate: TextureView,
    private val analyticManager: AnalyticsManager,
    private val inspDatabase: InspDatabase,
    private val storyUnfinishedNotificationManager: StoryUnfinishedNotificationManager,
    private val kLoggerGetter: LoggerGetter,
    private val templateViewModel: TemplateViewModel,
    private val originalTemplateData: OriginalTemplateData?,
    val displayImageBitmap: (Bitmap?) -> Unit
) : RecordViewModel {

    private val logger: KLogger by lazy { kLoggerGetter.getLogger("export") }
    private val toGallerySaver: ToGallerySaver = ToGallerySaverImpl(logger)

    private var saveToGalleryJob: Job? = null
    private val surfaceState = MutableStateFlow<Surface?>(null)
    private var sentAnalytics: Boolean = saveState?.getBoolean(KEY_SENT_ANALYTICS) ?: false

    private val _stateSaveToGalleryUri = MutableStateFlow(saveState?.getString(
        KEY_SAVE_TO_GALLERY_URI
    )?.let { Uri.parse(it) })


    private var threadRecord: ThreadRecord? = null
    private val _state: MutableStateFlow<ExportState> =
        MutableStateFlow(retrieveInitialState(saveState, initialImageElseVideo))


    override val stateSaveToGalleryUri: StateFlow<Uri?> = _stateSaveToGalleryUri
    override val state: StateFlow<ExportState> = _state

    init {

        val stateVal = state.value

        if (stateVal !is ExportState.Rendered) {
            templateView.recordMode = RecordMode.UNSPECIFIED
        }

        coroutineScope.launch {

            combine(templateViewModel.template, state) { template, state ->
                if (template is InspResponseData) {
                    template.data to state
                } else null
            }.collect {

                val (template, state) = it ?: return@collect

                if (state is ExportState.UserPicked) {
                    onChoiceMadeWithoutChangeState(state, template)
                } else {
                    waitReadyToRender?.cancel()
                    waitReadyToRender = null

                    if (state is ExportState.Rendered) {
                        saveToGallery(state, template)
                    }
                }
            }
        }

        coroutineScope.launch {
            templateViewModel.template.collect {
                if (it is InspResponseData) {
                    val template = it.data
                    onTemplateDataLoaded(template)
                }
            }
        }

        coroutineScope.launch {
            combine(state, templateView.isInitialized) { state, isInitialized ->
                state is ExportState.Initial && isInitialized
            }.distinctUntilChanged()
                .filter { it }
                .collect {

                    if (BuildConfig.DEBUG && templateView.recordMode != RecordMode.UNSPECIFIED &&
                        templateView.recordMode != RecordMode.NONE
                    ) {
                        throw IllegalStateException(
                            "cannot be in record ${templateView.recordMode} here. " +
                                    "Recording is set only after user picked ${state.value} and the template is initialized"
                        )
                    }

                    templateView.setFrameForEdit()
                }
        }
    }

    /** conditions before rendering: Initialized. Listen for all necessary events.
     * Inner initializations
     * 1. template is loaded.
     * 2. textureIsAvailable.
     * 3. videoSeekIsCompleted
     *
     * User picked - can be any time long
     * 0. imageOrVideo picked
     * */
    private fun createSurfaceWhenTextureReady() {
        textureTemplate.doWhenAvailable(::onSurfaceTextureAvailable)
    }

    private val coroutineScope: CoroutineScope
        get() = activity.lifecycleScope

    private fun retrieveInitialState(
        bundle: Bundle?,
        initialImageElseVideo: Boolean?
    ): ExportState {

        val imageElseVideo =
            if (bundle?.containsKey(KEY_IMAGE_ELSE_VIDEO) == true) bundle.getBoolean(
                KEY_IMAGE_ELSE_VIDEO
            ) else initialImageElseVideo ?: false

        val whereToExport: WhereToExport? = bundle?.getParcelable(KEY_WHERE_TO_EXPORT)
        val renderedFile = bundle?.getString(KEY_RENDER_FINISHED_FILE, null)
        val fromDialog = bundle?.getBoolean(KEY_FROM_DIALOG, false) ?: false

        return when {
            renderedFile != null -> {
                ExportState.Rendered(imageElseVideo, whereToExport, fromDialog, renderedFile)
            }
            whereToExport != null -> {
                ExportState.UserPicked(imageElseVideo, whereToExport, fromDialog)
            }
            else -> ExportState.Initial(imageElseVideo)
        }
    }

    override fun stopRecordThread() {
        logger.debug { "stopRecording. thread = $threadRecord" }

        val threadRecord = threadRecord
        if (threadRecord != null) {
            threadRecord.onViewDestroyed()
            threadRecord.interrupt()
            this.threadRecord = null
        }
    }

    override fun sendAnalyticsShared(
        whereToExport: WhereToExport,
        fromDialog: Boolean
    ) {

        sentAnalytics = true
        analyticManager.shareTemplate(
            whereToExport.whereScreen,
            fromDialog,
            !state.value.imageElseVideo,
            templateView.template
        )

        coroutineScope.launch(Dispatchers.IO) {
            whereToExport.whereApp.let {
                if (it.isNotEmpty()) {
                    inspDatabase.shareItemQueries.insertItem(
                        it,
                        DateTime.nowUnixLong()
                    )
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {

        outState.putBoolean(KEY_SENT_ANALYTICS, sentAnalytics)

        val state = this.state.value
        outState.putBoolean(KEY_IMAGE_ELSE_VIDEO, state.imageElseVideo)

        if (state !is ExportState.Initial) {
            outState.putParcelable(KEY_WHERE_TO_EXPORT, state.whereToExport)
            outState.putBoolean(KEY_FROM_DIALOG, state.fromDialog)

            if (state is ExportState.Rendered) {
                outState.putString(KEY_RENDER_FINISHED_FILE, state.file)
            }
        }

        val savedToGalleryUri: Uri? = stateSaveToGalleryUri.value
        if (savedToGalleryUri != null)
            outState.putString(KEY_SAVE_TO_GALLERY_URI, savedToGalleryUri.toString())
    }

    private fun onSurfaceTextureAvailable(it: SurfaceTexture) {
        surfaceState.tryEmit(Surface(it))
    }

    private var waitReadyToRender: Job? = null

    private fun onChoiceMadeWithoutChangeState(state: ExportState.UserPicked, template: Template) {
        K.i(TAG_TEMPLATE) {
            "onChoiceMadeWithoutChangeState $state"
        }
        fun checkState() {
            if (BuildConfig.DEBUG) {

                if (templateView.recordMode != RecordMode.UNSPECIFIED)
                    throw IllegalStateException("wrong recordMode ${templateView.recordMode}")

            }
        }

        checkState()

        if (state.imageElseVideo) {
            templateView.recordMode = RecordMode.IMAGE
        } else {
            templateView.prepareToVideoRecording()
        }

        createSurfaceWhenTextureReady()

        waitReadyToRender = coroutineScope.launch {
            combine(
                templateView.isInitialized,
                surfaceState,
                templateView.needToWaitVideoSeek(),
                templateView.currentSize

            ) { isInitialized, surface, needToWaitVideo, size ->

                if (isInitialized && surface != null && !needToWaitVideo
                    && size == templateView.template.format.getRenderingSize()
                )
                    surface
                else
                    null
            }.distinctUntilChanged()
                .collect {
                    if (it != null) {
                        if (this@RecordViewModelImpl.state.value !is ExportState.UserPicked)
                            throw IllegalStateException("state should be UserPicked")

                        waitReadyToRender?.cancel()
                        waitReadyToRender = null
                        toRenderInProcessState(it, state, template)
                    }
                }
        }
    }

    override fun onChoiceMade(
        whereToExport: WhereToExport,
        fromDialog: Boolean,
        imageElseVideo: Boolean
    ) {
        _state.value = ExportState.UserPicked(imageElseVideo, whereToExport, fromDialog)
    }

    override fun onChangeImageElseVideo(imageElseVideo: Boolean) {
        _state.value = ExportState.Initial(imageElseVideo)
    }

    private fun startRenderTemplate(
        whereToExport: WhereToExport,
        fromDialog: Boolean, surface: Surface, template: Template
    ) {

        val file = File(activity.getExportFilesFolder(), "${getTemplateName(template)}.mp4")

        val recordListener = object : RecordListener {

            override fun onError(e: Throwable, isCritical: Boolean) {
                e.printCrashlytics()

                if (isCritical) {
                    threadRecord = null //remove reference.
                    file.delete()

                    templateView.post {
                        if (e !is InterruptedException) {
                            e.toastError()
                            activity.onBackPressed()
                        }
                    }
                } else {
                    templateView.post {
                        e.toastError()
                    }
                }
            }

            override fun onFinish(timeTook: Long) {
                threadRecord = null //remove reference.

                templateView.post {

                    logger.info {
                        "The template is finished to download, time took = ${timeTook}," + " speed = ${
                            String.format(
                                "%.2f",
                                (templateView.getDuration() * FRAME_IN_MILLIS) / timeTook.toDouble()
                            )
                        }," + " saved at path = ${file.absolutePath}"
                    }

                    toRenderedState(whereToExport, file)
                }
            }

            override fun onUpdate(progress: Float, encoderType: EncoderType) {
                templateView.post {
                    if (encoderType == EncoderType.VIDEO && surface.isValid) {
                        surface.lockHardwareCanvas()?.also {

                            if (templateView.viewWidth > 0) {
                                val scale = textureTemplate.width / templateView.viewWidth.toFloat()
                                it.scale(scale, scale)
                            }
                            innerGroupZView.draw(it)
                            surface.unlockCanvasAndPost(it)
                        }
                    }
                    _state.value =
                        ExportState.RenderingInProcess(
                            imageElseVideo = false,
                            progress = progress,
                            whereToExport = whereToExport,
                            fromDialog = fromDialog
                        )
                }
            }
        }

        threadRecord = ThreadRecord(
            templateView as InspTemplateViewAndroid,
            recordListener,
            file,
            innerGroupZView
        )
        threadRecord!!.start()
    }

    override fun onDestroy() {
        stopRecordThread()
        clearSurface()
        if (!sentAnalytics && state.value is ExportState.Rendered) {
            sendAnalyticsShared(WhereToExport("", "none"), false)
        }
    }


    private fun unsetRecording(file: File) {
        if (templateView.isRecording) {
            templateView.recordMode = RecordMode.NONE

            storyUnfinishedNotificationManager.onStoryRendered(file.absolutePath)

            //reload the template to avoid bugs.
            templateView.post {
                templateView.loadTemplate(templateView.template)
            }
        }
    }

    private fun onTemplateDataLoaded(
        template: Template
    ) {
        template.originalData = originalTemplateData

        try {
            templateView.loadTemplate(template)
        } catch (e: Exception) {
            Toast.makeText(
                activity,
                "Error to load template " + e.message,
                Toast.LENGTH_LONG
            )
                .show()
            e.printDebug()
        }
    }

    private fun toRenderInProcessState(
        surface: Surface,
        state: ExportState.UserPicked,
        template: Template
    ) {

        if (BuildConfig.DEBUG && templateView.recordMode != RecordMode.getForRecord(state.imageElseVideo)) {
            throw IllegalStateException(
                "wrong recordMode ${templateView.recordMode}, expected ${
                    RecordMode.getForRecord(
                        state.imageElseVideo
                    )
                }"
            )
        }

        val whereToExport = state.whereToExport
        _state.value =
            (ExportState.RenderingInProcess(
                state.imageElseVideo,
                whereToExport, state.fromDialog, null
            ))

        assertEquals(
            templateView.getSize(),
            templateView.template.format.getRenderingSize()
        )

        if (state.imageElseVideo) {
            saveTemplateAsImage(whereToExport, surface, template)
        } else {
            startRenderTemplate(whereToExport, state.fromDialog, surface, template)
        }
    }

    private fun getTemplateName(template: Template) = template.getNameForShare()

    override fun getMimeType(imageElseVideo: Boolean) =
        if (imageElseVideo) MIME_TYPE_IMAGE else MIME_TYPE_VIDEO

    private fun clearSurface() {
        surfaceState.value?.let {
            surfaceState.tryEmit(null)
            it.release()
        }
    }

    private fun toRenderedState(whereToExport: WhereToExport, file: File) {

        unsetRecording(file)

        clearSurface()

        val state = state.value

        _state.value = ExportState.Rendered(
            state.imageElseVideo,
            whereToExport,
            state.fromDialog,
            file.absolutePath
        )
    }

    private fun saveToGallery(state: ExportState.Rendered, template: Template) {

        if (_stateSaveToGalleryUri.value == null && saveToGalleryJob == null) {
            saveToGalleryJob = coroutineScope.launch {
                val uri =
                    toGallerySaver.saveToGalleryAsync(
                        activity,
                        state.imageElseVideo,
                        File(state.file),
                        getMimeType(state.imageElseVideo),
                        template.getNameForShare()
                    )
                _stateSaveToGalleryUri.emit(uri)
            }
        }
    }

    private fun saveTemplateAsImage(
        whereToExport: WhereToExport,
        surface: Surface,
        template: Template
    ) {

        val time = System.currentTimeMillis()

        K.i(TAG_CODEC) {
            "saveTemplateAsImage, inside of post"
        }

        innerGroupZView.post {

            val frameForEdit = templateView.getFrameForEdit()

            K.i(TAG_CODEC) {
                "saveTemplateAsImage, frameForEdit $time"
            }

            coroutineScope.launch(Dispatchers.Main) {

                templateView.setFrameSync(frameForEdit)
                withContext(Dispatchers.IO) {
                    templateView.prepareAnimation(frameForEdit)
                    templateView.setVideoFrameSync(frameForEdit, false)
                }

                surface.lockHardwareCanvas()?.also {
                    //it.scale(scale, scale)
                    innerGroupZView.draw(it)
                    surface.unlockCanvasAndPost(it)
                }

                // otherwise the bitmap is black on huawei phones
                delay(50L)
                val bitmap = textureTemplate.getBitmap(
                    templateView.viewWidth,
                    templateView.viewHeight
                )

                displayImageBitmap(bitmap)

                val file = withContext(Dispatchers.IO) {
                    saveBitmapAsJpeg(activity, bitmap!!, template)
                }

                val timeTook = System.currentTimeMillis() - time

                K.i(TAG_CODEC) {
                    "The template is finished to download, time took = ${timeTook}," +
                            " saved at path = ${file.absolutePath}, " +
                            "width ${templateView.viewWidth}, height ${templateView.viewHeight}"
                }

                toRenderedState(whereToExport, file)
            }
        }
    }

    private fun saveBitmapAsJpeg(context: Context, bitmap: Bitmap, template: Template): File {

        val file = File(context.getExportFilesFolder(), "${getTemplateName(template)}.jpeg")

        var stream: OutputStream? = null
        try {
            stream = file.outputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)

        } catch (e: Exception) {
            e.printDebug()
            file.delete()
        } finally {
            stream?.closeQuietly()
        }

        return file
    }
}

const val MIME_TYPE_IMAGE = "image/jpeg"
const val MIME_TYPE_VIDEO = "video/mp4"
const val KEY_IMAGE_ELSE_VIDEO = "image_else_video"
private const val KEY_RENDER_FINISHED_FILE = "renderFinishedFile"
private const val KEY_FROM_DIALOG = "fromDialog"
private const val KEY_SAVE_TO_GALLERY_URI = "saveToGalleryUri"
private const val KEY_WHERE_TO_EXPORT = "saveToGalleryUri"
private const val KEY_SENT_ANALYTICS = "sent_analytics"


fun Context.getExportFilesFolder(create: Boolean = true) =
    File(cacheDir, "export").also { if (create) it.mkdirs() }

fun Context.getLottieFilesFolder(create: Boolean = true) =
    File(cacheDir, "lottie").also { if (create) it.mkdirs() }

fun TextureView.doWhenAvailable(action: (SurfaceTexture) -> Unit) {
    if (isAvailable) {
        action(surfaceTexture!!)
    } else {
        surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                action(surface)
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
    }
}

fun <T> assertEquals(actual: T, expected: T) {
    if (actual != expected) {
        throw IllegalStateException("actual = $actual, expected $expected")
    }
}