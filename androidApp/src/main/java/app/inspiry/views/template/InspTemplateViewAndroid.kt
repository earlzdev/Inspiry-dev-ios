package app.inspiry.views.template

import android.graphics.Outline
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewOutlineProvider
import androidx.activity.ComponentActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.*
import app.inspiry.BuildConfig
import app.inspiry.R
import app.inspiry.core.data.Size
import app.inspiry.core.helper.PlayTemplateFlow
import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.Media
import app.inspiry.core.media.Template
import app.inspiry.core.opengl.programPresets.TextureMaskProvider
import app.inspiry.core.template.TemplateReadWrite
import app.inspiry.font.helpers.TextCaseHelper
import app.inspiry.font.provider.FontsManager
import app.inspiry.helpers.K
import app.inspiry.music.android.client.ExoAudioPlayer
import app.inspiry.music.client.BaseAudioPlayer
import app.inspiry.palette.model.AbsPaletteColor
import app.inspiry.palette.util.getDrawable
import app.inspiry.utils.TAG_TEMPLATE
import app.inspiry.utils.dpToPixels
import app.inspiry.utils.removeOnClickListener
import app.inspiry.video.player.controller.RecordableVideoPlayerController
import app.inspiry.video.renderThread
import app.inspiry.views.InspParent
import app.inspiry.views.InspView
import app.inspiry.views.androidhelper.InspLayoutParams.Companion.TAG_INSP_VIEW
import app.inspiry.views.androidhelper.createLayoutParams
import app.inspiry.views.factory.ViewFromMediaAndroidFactory
import app.inspiry.views.group.BaseGroupZView
import app.inspiry.views.guideline.GuideLineAndroid
import app.inspiry.views.guideline.GuidelineManagerAndroid
import app.inspiry.views.infoview.InfoViewModel
import app.inspiry.views.media.InnerMediaViewAndroid
import app.inspiry.views.touch.MovableTouchHelperFactoryImpl
import app.inspiry.views.viewplatform.getAndroidView
import com.google.android.exoplayer2.upstream.cache.Cache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onCompletion
import kotlinx.serialization.json.Json
import okio.FileSystem


class InspTemplateViewAndroid(
    val innerView: BaseGroupZView,
    private val exoPlayerCache: Cache,
    loggerGetter: LoggerGetter,
    unitsConverter: BaseUnitsConverter,
    infoViewModel: InfoViewModel?,
    json: Json,
    textCaseHelper: TextCaseHelper,
    fontsManager: FontsManager,
    templateSaver: TemplateReadWrite,
    fileSystem: FileSystem,
    initialDisplayMode: TemplateMode
) : InspTemplateView(
    loggerGetter,
    unitsConverter,
    infoViewModel,
    json,
    textCaseHelper,
    fontsManager,
    templateSaver,
    GuidelineManagerAndroid(),
    ViewFromMediaAndroidFactory(innerView.context),
    MovableTouchHelperFactoryImpl(),
    fileSystem, initialDisplayMode
) {

    private val localHandler: Handler = Handler(Looper.getMainLooper())

    override val viewWidth: Int
        get() = innerView.width
    override val viewHeight: Int
        get() = innerView.height

    override val viewScope: CoroutineScope?
        get() = innerView.findViewTreeLifecycleOwner()?.lifecycleScope

    override val containerScope: CoroutineScope =
        (innerView.context as ComponentActivity).lifecycleScope

    override val currentSize: MutableStateFlow<Size?> = MutableStateFlow(getSize())

    init {
        initInnerView(innerView)
    }

    private fun initInnerView(innerView: BaseGroupZView) {
        innerView.setWillNotDraw(false)
        setLifecycle(innerView)

        innerView.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            currentSize.value = getSize()
            logger.debug { "onSizeChanged:: ${currentSize.value}" }
        }

        innerView.onDrawForeground = { canvas ->
            if (templateMode == TemplateMode.EDIT) {
                guidelines.onEach {
                    (it as GuideLineAndroid).onDraw(canvas, guidelineManager)
                }
            }
        }

        if (templateMode == TemplateMode.EDIT) {
            containerScope.launch {
                templateTransform.collect { transform ->
                    updateTemplatePosition(transform = transform)
                }
            }
        }
    }

    private fun updateTemplatePosition(transform: TemplateTransform) {

        val parentHeight = (innerView.parent as? ConstraintLayout)?.height
            ?: -1 //using only with centerGravity > 0

        val width = transform.containerSize.width
        val height = width / transform.aspectRatio

        val y = (((height - (height * transform.scale)) / -2f) + transform.verticalOffset)
        val offset = y + transform.staticOffset +
                ((parentHeight - height) / 2f - y - transform.staticOffset) * transform.centerGravity
        val realScale = (1f - transform.scale) * transform.centerGravity + transform.scale

        with(innerView) {
            scaleX = realScale
            scaleY = realScale
            translationY = offset
        }
    }

    private fun setLifecycle(innerView: BaseGroupZView) {
        (innerView.context as? LifecycleOwner)?.lifecycle?.addObserver(LifecycleEventObserver { _: LifecycleOwner, event: Lifecycle.Event ->

            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    onResumeOwner()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    onPauseOwner()
                }
                Lifecycle.Event.ON_STOP -> {
                    onStopOwner()
                }
                Lifecycle.Event.ON_START -> {
                    onStartOwner()
                }
                else -> {}
            }
        })
    }

    override fun startPlayingJob() {
        playingJob = viewScope?.launch {
            PlayTemplateFlow.create(currentFrame, maxFrames, loopAnimation)
                .onCompletion { playingJob = null }
                .catch { playingJob = null }
                .collect {
                    onFrameUpdated(it)
                }
        }
    }

    override val copyInspViewPlusTranslation: Float
        get() = COPY_INSP_VIEW_TRANSLATION_PLUS.dpToPixels()

    override fun setBackgroundColor(color: AbsPaletteColor?) {
        innerView.background = color?.getDrawable()
    }

    override fun prepareAnimation(frame: Int) {
        if (BuildConfig.DEBUG && Looper.getMainLooper().isCurrentThread) {
            throw IllegalStateException("Wrong thread. Should be background thread")
        }
        super.prepareAnimation(frame)
    }

    override fun addInspView(it: Media, parentInsp: InspParent, simpleVideo: Boolean): InspView<*> {

        val res = super.addInspView(it, parentInsp, simpleVideo)
        res.getAndroidView().also {
            it.layoutParams = parentInsp.createLayoutParams(res.media.layoutPosition)
            it.setTag(TAG_INSP_VIEW, res)
        }

        return res
    }

    override fun addViewToHierarchy(view: InspView<*>) {
        innerView.addView(view.getAndroidView())
    }

    override fun addViewToHierarchy(index: Int, view: InspView<*>) {
        innerView.addView(view.getAndroidView(), index)
    }

    override fun removeViews() {
        super.removeViews()
        innerView.removeAllViews()
    }

    override fun invalidateGuidelines() {
        innerView.invalidate()
    }

    override fun initMusicPlayer(): BaseAudioPlayer {
        return ExoAudioPlayer(innerView.context, exoPlayerCache)
    }

    override fun setBackgroundColor(color: Int) {
        innerView.setBackgroundColor(color)
    }

    override fun loadTemplate(template: Template) {
        innerView.clipChildren = template.clipChildren
        super.loadTemplate(template)

    }

    override fun onTemplateModeHasChanged(newMode: TemplateMode, reallyChanged: Boolean) {
        super.onTemplateModeHasChanged(newMode, reallyChanged)
        logger.info { "onTemplateModeHasChanged ${newMode}, reallyChanged ${reallyChanged}" }

        if (templateMode == TemplateMode.EDIT)
            innerView.setOnClickListener { changeSelectedView(null) }
        else if (reallyChanged) {
            innerView.removeOnClickListener()
        }
    }

    override fun innerFinishInitializing() {
        //to ensure everything laid out correctly...
        post {
            super.innerFinishInitializing()
        }
    }

    private val _waitVideoSeek = MutableStateFlow(0)
    override val waitVideoSeek = _waitVideoSeek.asStateFlow()

    private fun setVideoFrameRefreshOnSeek(frame: Int) {
        //already waiting
        if (this.waitVideoSeek.value != 0) {
            K.i(TAG_TEMPLATE) {
                "waitVideoSeek already waiting ${this.waitVideoSeek.value} or isRecording ${isRecording}"
            }
            return
        }

        if (BuildConfig.DEBUG && frame != 0)
            throw IllegalStateException("frame cannot be other than 0 - ${frame}")

        mediaViews.forEach {

            val innerMediaView = it.innerMediaView as InnerMediaViewAndroid

            if (it.hasVideo()) {
                innerMediaView.executePlayers {

                    if (!_waitVideoSeek.tryEmit(_waitVideoSeek.value + 1))
                        throw IllegalStateException("should always return true")

                    (it as RecordableVideoPlayerController).onSeekFinished = {

                        var waitVideoSeek = this.waitVideoSeek.value
                        waitVideoSeek--

                        if (waitVideoSeek == 0) {
                            onVideoSeekFinished()
                        } else {
                            this._waitVideoSeek.tryEmit(waitVideoSeek)
                        }
                    }
                    it.drawFrame(frame, false)
                }
            }
        }
        K.i(TAG_TEMPLATE) {
            "setVideoFrameRefreshOnSeek ${frame}, waitVideoSeeks ${this.waitVideoSeek.value}"
        }

        if (this.waitVideoSeek.value != 0 && VIDEO_SEEK_TIMEOUT_MILLIS != 0L) {
            localHandler.postDelayed(seekTimedOut, VIDEO_SEEK_TIMEOUT_MILLIS)
        }
    }

    private val seekTimedOut: Runnable = Runnable {
        mediaViews.forEach {
            (it.innerMediaView as InnerMediaViewAndroid).executePlayers {
                (it as? RecordableVideoPlayerController?)?.onSeekFinished = null
            }
        }
        onVideoSeekFinished()
    }

    private fun onVideoSeekFinished() {
        K.i(TAG_TEMPLATE) {
            "onVideoSeekFinished $waitVideoSeek"
        }
        localHandler.removeCallbacks(seekTimedOut)
        localHandler.post {
            mediaViews.forEach {
                val innerMediaView = it.innerMediaView as InnerMediaViewAndroid
                innerMediaView.redrawProgram()
            }

            //in this way we insure that it is called after we redraw the program
            if (waitVideoSeek.value != 0) {
                renderThread {
                    localHandler.post {
                        _waitVideoSeek.tryEmit(0)

                        K.i(TAG_TEMPLATE) {
                            "onVideoSeekFinished render -> localHandler -> invoked"
                        }
                    }
                }
            } else {
                K.i(TAG_TEMPLATE) {
                    "onVideoSeekFinished listener was null"
                }
                _waitVideoSeek.tryEmit(0)
            }
        }
    }

    override fun removeViewFromHierarchy(view: InspView<*>, removeFromTemplateViews: Boolean) {
        super.removeViewFromHierarchy(view, removeFromTemplateViews)
        innerView.removeView(view.getAndroidView())
    }

    override fun changeOrderOfViews(first: Media, second: Media) {
        super.changeOrderOfViews(first, second)

        //use translationZ instead of heavy operations of changing order
        inspChildren.forEach {
            it.getAndroidView().bringToFront()
        }
    }

    override fun isWindowVisible(): Boolean {
        return (innerView.context as ComponentActivity).lifecycle.currentState.isAtLeast(
            Lifecycle.State.STARTED
        )
    }


    override fun post(action: () -> Unit) {
        localHandler.post(action)
    }

    override fun setFrameAsInitial(frame: Int) {
        super.setFrameAsInitial(frame)

        if (recordMode.started)
            setVideoFrameRefreshOnSeek(frame)
        else {
            setVideoFrameAsync(frame, false)
        }

        //ugly hack necessary to draw gl template textures in a correct state
        if (mediaViews.any { it.media.hasProgram() }) {
            localHandler.post {
                setFrameSyncInner(currentFrame)
            }
        }
    }
}

// 0 if none
private val VIDEO_SEEK_TIMEOUT_MILLIS = if (BuildConfig.DEBUG) 0L else 3000L

fun View.setTemplateRoundedCornersAndShadow() {
    clipToOutline = true
    val roundedCorners = resources.getDimensionPixelSize(R.dimen.item_template_corner_radius)
        .toFloat()
    outlineProvider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            outline.setRoundRect(Rect(0, 0, view.width, view.height), roundedCorners)
            outline.alpha = 0.4f
        }
    }
    elevation = 3f.dpToPixels()
    translationZ = 6f.dpToPixels()
}
