package app.inspiry.views.media

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Build
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.WorkerThread
import androidx.core.graphics.alpha
import androidx.core.view.children
import androidx.core.view.doOnLayout
import androidx.lifecycle.findViewTreeLifecycleOwner
import app.inspiry.BuildConfig
import app.inspiry.R
import app.inspiry.animator.helper.AnimationHelperAndroid
import app.inspiry.core.animator.appliers.BlurAnimApplier
import app.inspiry.core.data.SizeF
import app.inspiry.core.data.frameToTimeUs
import app.inspiry.core.media.*
import app.inspiry.core.util.ArgbColorManager
import app.inspiry.helpers.K
import app.inspiry.helpers.TouchMediaMatrixHelper
import app.inspiry.media.toJava
import app.inspiry.utils.dpToPixels
import app.inspiry.utils.getColorCompat
import app.inspiry.utils.printCrashlytics
import app.inspiry.utils.printDebug
import app.inspiry.video.CustomTextureView
import app.inspiry.video.egl.EGLOutput
import app.inspiry.core.data.TransformMediaData
import app.inspiry.video.parseAssetsPathForAndroid
import app.inspiry.video.player.controller.GlVideoPlayerController
import app.inspiry.video.player.controller.RealtimeVideoPlayerController
import app.inspiry.video.player.controller.RecordableVideoPlayerController
import app.inspiry.video.player.decoder.TextureSize
import app.inspiry.video.program.DefaultProgramCreator
import app.inspiry.video.program.DeferredProgramCreator
import app.inspiry.views.androidhelper.*
import app.inspiry.views.group.InspGroupViewAndroid
import app.inspiry.views.template.TemplateMode
import app.inspiry.views.template.forEachRecursive
import app.inspiry.views.touch.MovableTouchHelperAndroid
import app.inspiry.views.viewplatform.getAndroidView
import coil.ImageLoader
import coil.request.Disposable
import coil.request.ImageRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.math.roundToInt

@SuppressLint("ViewConstructor")
class InnerMediaViewAndroid(context: Context, val media: MediaImage) : FrameLayout(context),
    InnerMediaView,
    EGLOutput.RenderingPreparedListener, TouchMediaMatrixHelper.OnMatrixChangedListener,
    KoinComponent {

    //TODO: circular dependency is not good
    lateinit var mediaView: InspMediaView

    private var textureView: CustomTextureView? = null

    // it is also true if we don't have a surface
    private var isSurfacePrepared: Boolean = true

    private var deferredProgramCreator: DeferredProgramCreator? = null
    private var needToRestoreRendering = true

    var touchMediaMatrixHelper: TouchMediaMatrixHelper? = null
    private var videoPreparedRunnable: Runnable? = null
    private var isTextureViewTemporaryScaleEnabled = false

    @Volatile
    var isRenderingPrepared: Boolean? = null
        private set

    var sizeIsKnown: Boolean = false

    /**
     * these fields are used for interruption of loading
     */
    var lastLoadImageTarget: Disposable? = null

    override var framePreparedCallback: (() -> Unit)? = null

    private val canvasUtils: CanvasUtils by lazy { CanvasUtils() }

    val imageView: BaseBlurImageView =
        if (Build.VERSION.SDK_INT >= 31) BlurImageViewS(context)
        else BlurImageViewCompat(context)

    val imageLoader: ImageLoader by inject()

    init {

        setClipProps()

        addView(
            imageView.wrapFrameLayout(), LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        setWillNotDraw(false)

        addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->

            sizeIsKnown = true
            setInnerPivots()
        }
    }

    private fun setClipProps() {
        this.clipToOutline = true
        this.clipChildren = true
        this.clipToPadding = true
    }

    private val invalidateRunnable = Runnable {
        if (isAttachedToWindow) invalidate()
    }

    override fun playVideoIfExists(forcePlay: Boolean) {
        executePlayers { it.play(mediaView.currentFrame, forcePlay) }
    }

    override fun pauseVideoIfExists() {
        executePlayers { it.pause() }
    }

    override fun restartVideoIfExists() {
        executePlayers { it.restart(mediaView.currentFrame) }
    }

    override fun isVideoPlayingState(): StateFlow<Boolean> {

        var st: StateFlow<Boolean>? = null
        executePlayers(onlyForEditTexture = true) {
            st = (it as RealtimeVideoPlayerController).getIsPlayingState()
        }
        return st ?: MutableStateFlow(false)

    }

    override fun videoCurrentTimeMs(): StateFlow<Long> {
        var st: StateFlow<Long>? = null

        executePlayers(onlyForEditTexture = true) {
            st = (it as RealtimeVideoPlayerController).getCurrentTimeMs()
        }
        return st ?: MutableStateFlow(0L)
    }

    private fun getPorterDuffMode(): PorterDuff.Mode {
        return when (media.colorFilterMode) {
            ColorFilterMode.ADD -> PorterDuff.Mode.ADD
            ColorFilterMode.DARKEN -> PorterDuff.Mode.DARKEN
            ColorFilterMode.LIGHTEN -> PorterDuff.Mode.LIGHTEN
            ColorFilterMode.SCREEN -> PorterDuff.Mode.SCREEN
            ColorFilterMode.OVERLAY -> PorterDuff.Mode.OVERLAY
            ColorFilterMode.MULTIPLY -> PorterDuff.Mode.MULTIPLY
            else -> throw IllegalStateException("unknown color filter mode ${media.colorFilterMode}")
        }
    }

    override fun setColorFilter(color: Int?) {
        if (color == null || media.colorFilterMode == ColorFilterMode.DISABLE) {
            imageView.colorFilter = null
            return
        }
        if (media.colorFilterMode == ColorFilterMode.DEFAULT)
            imageView.setColorFilter(color)
        else imageView.setColorFilter(color, getPorterDuffMode())
    }

    override fun setPickImage(onClick: (() -> Unit)?) {
        imageView.scaleType = ImageView.ScaleType.CENTER_INSIDE
        imageView.setImageResource(R.drawable.icon_add)
        if (media.makeMovableWhenRemoveBg && media.removeBgOnInsert) {

            val drawable = GradientDrawable()
            drawable.setColor(
                ArgbColorManager.applyAlphaToColor(
                    context.getColorCompat(R.color.addImageBg),
                    0.96f
                )
            )
            drawable.cornerRadius = 8.dpToPixels()
            imageView.background = drawable
        } else {
            imageView.setBackgroundColor(context.getColorCompat(R.color.addImageBg))
        }

        if (onClick != null)
            setOnClickListener { onClick() }
    }

    override fun setImageInitial(
        url: String?, onError: (Throwable?) -> Unit, onSuccess: () -> Unit
    ) {

        imageView.scaleType = mediaView.getImageScaleType().toJava()
        imageView.setBackgroundResource(0)

        if (url != null) {
            doWhenSizeIsKnown {
                loadImage(
                    uri = url.parseAssetsPathForAndroid(),
                    onSuccess = onSuccess,
                    onError = onError
                )
            }
        } else {
            deferredProgramCreator = null
            setupImage(null, null)
        }
    }


    override fun setDisplayVideo(): Boolean {
        if (deferredProgramCreator != null) {
            startRendering(deferredProgramCreator!!)
            return true
        }
        return false
    }


    override fun getVideoDurationMs(): Long {
        var duration = 0L
        executePlayers(onlyForEditTexture = true) {
            duration = it.videoInfo?.videoDurationUs?.let { it / 1000L } ?: 0L
        }
        return duration
    }

    override fun setUpMatrix() {
        touchMediaMatrixHelper?.setupMatrix(
            TransformMediaData(
                media.demoScale,
                media.demoOffsetX,
                media.demoOffsetY,
                media.innerImageRotation
            )
        )
    }

    //this method can be called alone (without startRendering) if we do onStop, onStart
    @WorkerThread
    override fun onSurfacePrepareCompleted() {

        videoPreparedRunnable = Runnable {
            mediaView.templateParentNullable?.childHasFinishedInitializing(mediaView)
            isRenderingPrepared = true
        }
        postDelayed(videoPreparedRunnable, VIDEO_PREPARED_TIMEOUT)
        isSurfacePrepared = true

        if (media.hasVideo() && mediaView.templateParentNullable?.isInitialized?.value == true) {
            post {
                mediaView.templateParentNullable?.let { it.setFrameAsInitial(it.currentFrame) }
            }
        }
    }

    override fun onPrepareFailed(e: Exception) {
        K.d("InnerMediaViewAndroid") {
            "onPrepareFailed ${e}, isRenderingPrepared ${isRenderingPrepared}"
        }
        isRenderingPrepared = false
        post {

            K.d("InnerMediaViewAndroid") {
                "onPrepareFailedAfter ${e}, isRenderingPrepared ${isRenderingPrepared}, " +
                        "templateParent ${mediaView.templateParentNullable != null}"
            }
            if (isRenderingPrepared == false) {
                removeTextureView()
                mediaView.templateParentNullable?.showErrorView(e)
            }
        }
    }

    private fun setupImage(bitmap: Bitmap?, uri: String?) {

        var textureRotation: Float? = null
        media.isVideo = false

        removeDefaultVideoRendering()
        // Set texture as Bitmap if need
        val programCreator = deferredProgramCreator

        if (programCreator != null) {
            programCreator.setImage(bitmap, 0, uri)
            startRendering(programCreator)
            textureRotation = 0f //for correctly rotating the texture using gestures
        } else {
            imageView.setImageBitmap(bitmap)
            mediaView.templateParentNullable?.childHasFinishedInitializing(mediaView)
        }
        if (media.isMovable.nullOrFalse() && bitmap != null)
            setupTouchMedia(
                bitmap.width.toFloat(),
                bitmap.height.toFloat(),
                rotation = textureRotation
            )
    }

    /**
     * Restore last rendering after detach from window
     */
    private fun restoreRendering() {
        if (!needToRestoreRendering) return
        val programCreator = deferredProgramCreator ?: return
        val textureView = this.textureView
        if (textureView == null) startRendering(programCreator)
        else {
            isRenderingPrepared = false
            val templateParent = mediaView.templateParent
            templateParent.waitInitialize(mediaView)
            EGLOutput.register(textureView, programCreator, this, templateParent.isRecording)
        }
    }

    /**
     * Used to restore rendering in list after fast double scroll
     */
    override fun restoreRenderingInList() {
        if (!needToRestoreRendering) return

        val programCreator = deferredProgramCreator ?: return
        startRendering(programCreator)
    }

    override fun setTranslateInner(translationX: Float, translationY: Float) {
        imageView.translationX = translationX
        imageView.translationY = translationY
        textureView?.let {
            it.translationX = translationX
            it.translationY = translationY
        }
    }

    override fun setRecording(value: Boolean) {
        textureView?.let {
            EGLOutput.setRecording(it, value)
        }
    }

    override fun drawVideoFrameAsync(frame: Int, sequential: Boolean) {
        executePlayers {
            it.drawFrame(frame, sequential)
        }
    }

    override fun setVideoInner(uri: String, textureIndex: Int) {
        imageView.setBackgroundColor(0) //remove placeholder backround
        if (deferredProgramCreator == null)
            deferredProgramCreator = DeferredProgramCreator(getDefaultProgramCreator(uri))
        setVideoInProgramCreator(uri, textureIndex)

        removeTouchMedia()
        startRendering(deferredProgramCreator!!)
    }

    override fun doWhenSizeIsKnown(function: () -> Unit) {
        if (sizeIsKnown) {
            function()
        } else {
            doOnLayout {
                function()
            }
        }
    }

    override fun isVideoHasAudio(): Boolean {
        var res = false

        executePlayers(onlyForEditTexture = true) {
            res = it.hasAudioTrack()
        }

        return res
    }


    override fun loadNewImage(path: String, textureIndex: Int, onSuccess: () -> Unit) {

        loadImage(path.parseAssetsPathForAndroid(), builder = {
            error(ColorDrawable(0xffE0A5A5.toInt()))

        }, onSuccess = {

            imageView.scaleType = mediaView.getImageScaleType().toJava()
            imageView.setBackgroundResource(0)
            onSuccess()

        }, onError = {

            Toast.makeText(
                context,
                "Error to load media, click to try again $path, ${it?.message}",
                Toast.LENGTH_LONG
            ).show()
            setOnClickListener {
                mediaView.setPickImage()
            }

        })
    }

    private fun getTemplateTextures(): List<Media> {
        val medias = mutableListOf<Media>()

        mediaView.templateParent.template.medias.forEachRecursive {
            if (it.textureIndex != null && it is MediaGroup) {
                medias.add(it)
            }
        }
        return medias
    }

    private fun startRendering(programCreator: DeferredProgramCreator) {

        if (textureView != null || mediaView.templateParentNullable == null) return
        needToRestoreRendering = true

        if (!programCreator.isReady || !isAttachedToWindow) return

        programCreator.setTransform(
            TransformMediaData(
                media.innerImageScale, media.innerImageOffsetX,
                media.innerImageOffsetY, media.innerImageRotation
            )
        )
        imageView.setImageBitmap(null)
        val isTextureViewNew = textureView == null

        val templateParent = mediaView.templateParent

        val textureView = this.textureView
            ?: CustomTextureView(context).also {
                isRenderingPrepared = false
                templateParent.waitInitialize(mediaView)
                isSurfacePrepared = false
                it.isOpaque = false
                this.textureView = it
            }
        if (isTextureViewNew) addView(
            textureView.wrapFrameLayout(),
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        EGLOutput.register(textureView, programCreator, this, templateParent.isRecording)
    }

    fun hasBlurAnimation() = media.isHasAnimApplier<BlurAnimApplier>()

    /**
     * In editMode we use default program (without mask) for playing of video
     */
    private fun getDefaultProgramCreator(source: String) =
        DefaultProgramCreator.getDefaultProgramCreator(
            source, hasBlurAnimation(), media.isLoopEnabled
                ?: false
        )

    /**
     * Double check used to avoid unnecessary calls
     */
    private fun setRenderHasFinishedInitializingOnce() {

        if (isRenderingPrepared != true) {

            isRenderingPrepared = true
            post {
                if (isRenderingPrepared == true) {
                    videoPreparedRunnable?.let { removeCallbacks(it) }
                    mediaView.templateParentNullable?.childHasFinishedInitializing(mediaView)
                }
            }
        }
    }

    private fun removeTouchMedia() {
        touchMediaMatrixHelper?.run {
            touchMediaMatrixHelper = null
            unregisterListener()
        }
    }

    private fun mayInitProgramCreatorAndTemplateTextures() {
        if (media.programCreator != null) {
            deferredProgramCreator = DeferredProgramCreator(media.programCreator!!)

            getTemplateTextures().forEach {
                deferredProgramCreator!!.setTemplate(
                    it.view!!.getAndroidView() as ViewGroup,
                    it.textureIndex!!
                )
            }
        }
    }

    override fun refresh() {
        if (sizeIsKnown) {
            setInnerPivots()
        }
        mayInitProgramCreatorAndTemplateTextures()
    }

    override fun setVideoTotalDurationMs(duration: Int) {
        executePlayers {
            it.updateDecoderParamsAsync {
                it.totalDurationUs = duration * 1000L
            }
        }
    }

    override fun removeInnerMedia() {
        removeTextureView()
        if (media.isMovable != true) mayInitProgramCreatorAndTemplateTextures()
    }

    override fun updateVideoCurrentTimeNoViewTimingMode() {
        executePlayers {
            (it as RealtimeVideoPlayerController).updateCurrentTimeNoViewTimingMode()
        }
    }


    fun setInnerPivots() {

        if (media.innerPivotX != 0.5f || media.innerPivotY != 0.5f) {

            imageView.pivotX = media.innerPivotX * width
            pivotX = media.innerPivotX * width

            imageView.pivotY = media.innerPivotY * height
            pivotY = media.innerPivotY * height
        }
    }

    private fun View.wrapFrameLayout() =
        FrameLayout(context).also {
            it.clipChildren = true
            it.clipToPadding = true
            it.clipToOutline = true
            it.addView(
                this, LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            )
        }


    private fun loadImage(
        uri: String,
        builder: ImageRequest.Builder.() -> Unit = {},
        onSuccess: () -> Unit,
        onError: (Throwable?) -> Unit
    ) {
        val requestBuilder = ImageRequest.Builder(context)
        requestBuilder.builder()
        requestBuilder.crossfade(false)
            //force disabling HARDWARE bitmaps
            //this fixes bug with some OpenGL templates
            //where a bitmap texture was invisible or duplicated another texture
            .allowHardware(false)
            .data(Uri.parse(uri))
            .allowConversionToBitmap(true)

        val decreaseImageSize = Build.VERSION.SDK_INT < 31 && hasBlurAnimation()
        val width = media.getScaledSize(
            mediaView.unitsConverter,
            width,
            forWidth = true,
            decreaseImageSize
        )
        val height = media.getScaledSize(
            mediaView.unitsConverter,
            height,
            forWidth = false,
            decreaseImageSize
        )
        if (width > 0 && height > 0)
            requestBuilder.size(width, height)

        val lifecycle = findViewTreeLifecycleOwner()
        if (lifecycle != null)
            requestBuilder.lifecycle(lifecycle)
        requestBuilder.listener(onError = { request, throwable ->
            lastLoadImageTarget = null
            throwable.throwable.printDebug()
            post {
                onError(throwable.throwable)
            }
        }, onCancel = { lastLoadImageTarget = null },
            onSuccess = { request, metadata ->

                lastLoadImageTarget = null // on error and success


            }).target(onSuccess = {

            post {
                if (it is BitmapDrawable) {
                    onSuccess()
                    setupImage(it.bitmap, uri)
                } else {
                    val error =
                        IllegalStateException("wrong result image class ${it.javaClass.simpleName}")
                    error.printCrashlytics()
                    onError(error)
                }

            }
        })

        lastLoadImageTarget = imageLoader.enqueue(requestBuilder.build())
    }

    /**
     * @return true if we don't need to handle clicks
     */
    private fun checkTouchAlphaThreshold(xTap: Float, yTap: Float): Boolean {
        if (!media.hasUserSource()
            || media.isVideo
            || media.touchAlphaThreshold == null
            || mediaView.isSelectedForEdit) return false
        val bmp = (this.imageView.drawable as? BitmapDrawable)?.bitmap ?: return false
        val xScale = bmp.width / this.width.toFloat()
        val yScale = bmp.height / this.height.toFloat()
        val bmpXPosition = (xTap * xScale).roundToInt()
        val bmpYPosition = (yTap * yScale).roundToInt()
        if (bmpXPosition < 0 || bmpYPosition < 0 || bmp.height < bmpXPosition || bmp.width < bmpYPosition) return false
        val a = bmp.getPixel(bmpXPosition, bmpYPosition).alpha / 255f
        return a <= media.touchAlphaThreshold!!
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (alpha == 0f) return false
        if (event.action == MotionEvent.ACTION_DOWN && checkTouchAlphaThreshold(event.x, event.y)
        ) {
            return false
        }

        if ((mediaView.movableTouchHelper as MovableTouchHelperAndroid?)?.onTouchMovable(event) != null)
            return true

        if (!media.isEditable) return super.onTouchEvent(event)
        if (isMediaTouchAvailable()) {

            if (event.actionMasked == MotionEvent.ACTION_DOWN && mediaView.mClipBounds != null &&
                !mediaView.mClipBounds!!.contains(event.x.toInt(), event.y.toInt())
            ) {
                return false
            }


            if (!mediaView.isSelectedForEdit) {
                mediaView.templateParent.changeSelectedView(mediaView)
            }
            setupTouchVideoIfNeed()
            return touchMediaMatrixHelper?.onTouchEvent(event) == true
        }
        return super.onTouchEvent(event)
    }


    private fun isMediaTouchAvailable() =
        mediaView.templateMode == TemplateMode.EDIT && !mediaView.templateParent.isPlaying.value &&
                (imageView.scaleType == ImageView.ScaleType.MATRIX || textureView != null)

    private fun setupTouchVideoIfNeed() {
        if (touchMediaMatrixHelper == null && mediaView.isVideo() && media.isMovable.nullOrFalse()) {
            val videoSize = getVideoSize() ?: return

            val rotation = videoSize.rotation
            val videoHeight = videoSize.height.toFloat()
            val videoWidth = videoSize.width.toFloat()

            setupTouchMedia(videoWidth, videoHeight, rotation)
        }
    }

    private fun getVideoSize(): TextureSize? {
        val videoUri = media.originalSource ?: return null
        val videoInfo = textureView?.let { EGLOutput.getVideoInfo(it, videoUri) }
        return videoInfo?.videoSize
    }

    /**
     * Used to setting up touch image/video event
     */
    private fun setupTouchMedia(mediaWidth: Float, mediaHeight: Float, rotation: Float?) {
        val mediaSize = SizeF(mediaWidth, mediaHeight)
        val displaySize = SizeF(
            width.toFloat() - paddingLeft - paddingRight,
            height.toFloat() - paddingBottom - paddingTop
        )

        val transformImageData = TransformMediaData(
            media.innerImageScale,
            media.innerImageOffsetX,
            media.innerImageOffsetY,
            media.innerImageRotation
        )

        touchMediaMatrixHelper = TouchMediaMatrixHelper(
            context, displaySize, mediaSize,
            this, transformImageData, rotation
        )
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        if (w > 0 && h > 0) {
            val displaySize = PointF(
                w.toFloat() - paddingLeft - paddingRight,
                h.toFloat() - paddingBottom - paddingTop
            )
            touchMediaMatrixHelper?.updateDisplaySize(
                displaySize, TransformMediaData(
                    media.innerImageScale,
                    media.innerImageOffsetX, media.innerImageOffsetY, media.innerImageRotation
                )
            )
        }
    }

    override fun onMatrixChanged(
        matrix: Matrix,
        transformMediaData: TransformMediaData,
        fromUser: Boolean
    ) {

        media.innerImageScale = transformMediaData.scale
        media.innerImageOffsetX = transformMediaData.translateX
        media.innerImageOffsetY = transformMediaData.translateY
        media.innerImageRotation = transformMediaData.rotate

        if (fromUser) {
            mediaView.templateParentNullable?.isChanged?.value = true

            mediaView.doForDuplicated {
                with(it.innerMediaView as InnerMediaViewAndroid) {
                    if (isMediaTouchAvailable()) setupTouchVideoIfNeed()
                    touchMediaMatrixHelper?.setupMatrix(transformMediaData)
                }
            }
        }

        textureView
            ?.let { EGLOutput.setTransform(it, transformMediaData) }
            ?: kotlin.run {
                imageView.imageMatrix = matrix
            }
        mediaView.invalidateParentIfTexture()
    }

    override fun onDrawForeground(canvas: Canvas) {
        super.onDrawForeground(canvas)
        if (media.borderWidth != null) {
            canvasUtils.drawBorder(
                canvas = canvas,
                borderWidthString = media.borderWidth!!,
                borderType = media.borderType ?: BorderStyle.outside,
                borderColor = media.borderColor
                    ?: mediaView.getTemplate().palette.mainColor?.getFirstColor() ?: Color.WHITE,
                width = width,
                height = height,
                layoutParams = layoutParams as InspLayoutParams,
                cornerRadiusPosition = media.cornerRadiusPosition,
                cornerRadius = mediaView.radius,
                paddingStart = paddingStart,
                paddingEnd = paddingEnd,
                paddingBottom = paddingBottom,
                paddingTop = paddingTop
            )
        }
    }

    override fun draw(canvas: Canvas) {
        (mediaView.animationHelper as AnimationHelperAndroid).drawAnimations(
            canvas,
            mediaView.currentFrame
        )
        super.draw(canvas)
    }

    override fun onDraw(canvas: Canvas) {
        textureView?.temporaryScale()
    }

    /**
     * TextureView onSurfaceTextureAvailable is not called (TextureView is invisible) if view has animationIn clip animation
     *  (canvas.clipRect called) without any scale animation (scaleX, scaleY not called)
     * This method need to avoid this issue
     * Need to call after drawAnimations
     */
    private fun TextureView.temporaryScale() {
        if (isSurfacePrepared) {
            if (isTextureViewTemporaryScaleEnabled) {
                if (scaleX == TEXTURE_VIEW_TEMPORARY_SCALE) scaleX = 1F
                if (scaleY == TEXTURE_VIEW_TEMPORARY_SCALE) scaleY = 1F
                isTextureViewTemporaryScaleEnabled = false
            }
        } else {
            if (scaleX == 1F) {
                scaleX = TEXTURE_VIEW_TEMPORARY_SCALE
                isTextureViewTemporaryScaleEnabled = true
            }
            if (scaleY == 1F) {
                scaleY = TEXTURE_VIEW_TEMPORARY_SCALE
                isTextureViewTemporaryScaleEnabled = true
            }
        }
    }


    /**
     * Used to remove openGL default video program (template without mask)
     */
    private fun removeDefaultVideoRendering() {
        if (media.programCreator == null) {
            removeTextureView()
        }
    }

    private fun removeTextureView(isRestore: Boolean = false) {

        K.i("removeTextureView") {
            "views before remove ${children.map { it::class.java.simpleName }.toList()}. " +
                    "viewParentToRemove ${textureView?.let { findViewParentToRemove(it) }}"
        }
        if (!isRestore) {
            deferredProgramCreator = null
        }
        textureView?.let {
            EGLOutput.removeTextureView(it)
            textureView = null
            if (!isRestore) needToRestoreRendering = false
            isSurfacePrepared = true

            removeView(findViewParentToRemove(it))
        }
        K.i("removeTextureView") {
            "views after remove ${children.map { it::class.java.simpleName }.toList()}"
        }
    }

    private fun findViewParentToRemove(view: View): View {
        var curView = view

        while (curView.parent !== this) {
            curView = curView.parent as View
        }

        return curView
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        restoreRendering()
        invalidateStepByStep()
    }

    /**
     * Need to avoid bugs with empty static templates
     */
    private fun invalidateStepByStep() {
        postDelayed(invalidateRunnable, 500) // Fast, often doesn't work
        postDelayed(invalidateRunnable, 1000) // Medium, may not work
        postDelayed(invalidateRunnable, 2500) // Slow, should always work
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(invalidateRunnable)
        super.onDetachedFromWindow()

        removeTextureView(true)
    }

    override fun setInnerImageScale(scaleX: Float, scaleY: Float) {
        imageView.scaleX = scaleX
        imageView.scaleY = scaleY
        textureView?.scaleX = scaleX
        textureView?.scaleY = scaleY
    }

    override fun setBlurRadius(blurRadius: Float, async: Boolean) {

        textureView
            ?.let { EGLOutput.setBlurRadius(it, blurRadius) }
            ?: kotlin.run {
                imageView.setBlurRadius(blurRadius, async)
            }
    }

    private fun setVideoInProgramCreator(uri: String, textureIndex: Int) {
        deferredProgramCreator!!.setVideo(
            uri,
            media.videoVolume?.value ?: 0f,
            media.getVideoTimeOffsetUs(),
            textureIndex,
            mediaView.getViewTimeOffsetUs(),
            mediaView.getDurationForTrimmingMillis() * 1000L
        )
    }

    override fun onFramePrepared() {
        /*K.i("InnerMediaViewAndroid") {
            "onFramePrepared had callback ${framePreparedCallback != null}," +
                    " isRenderingPrepared ${isRenderingPrepared}"
        }*/

        framePreparedCallback?.invoke()
        framePreparedCallback = null
        setRenderHasFinishedInitializingOnce()
    }


    fun getSurfaceForTemplate(root: InspGroupViewAndroid, textureIndex: Int): Surface? {

        return textureView?.let {
            EGLOutput.getSurfaceForTemplateTexture(
                it,
                root.getAndroidView() as ViewGroup,
                textureIndex
            )
        }
    }

    fun redrawProgram() {
        textureView?.let {
            EGLOutput.redrawProgram(it)
        }
    }

    override fun interruptImageLoading() {
        if (lastLoadImageTarget != null) {
            lastLoadImageTarget?.dispose()
            lastLoadImageTarget = null
            media.originalSource = null
            mediaView.setPickImage()
        }
    }

    override fun drawVideoFrameSync(frame: Int, sequential: Boolean) {
        executePlayers {
            (it as RecordableVideoPlayerController).drawFrameSync(frame.frameToTimeUs(), sequential)
        }
    }

    override fun updateVideoVolume(volume: Float) {
        executePlayers {
            it.updateDecoderParamsAsync {
                it.volume = volume
            }
        }
    }

    override fun setVideoPositionIgnoreViewTiming() {
        executePlayers {
            (it as RealtimeVideoPlayerController).setVideoPositionIgnoreViewTiming()
        }
    }

    override fun updateVideoStartTime(
        originalUri: String,
        textureIndex: Int,
        videoStartTimeMs: Int
    ) {
        setVideoInProgramCreator(originalUri, textureIndex)

        executePlayers(onlyForEditTexture = true) {
            it.updateDecoderParamsAsync {
                it.videoStartTimeUs = videoStartTimeMs * 1000L
            }
        }
    }

    fun executePlayers(
        onlyForEditTexture: Boolean = false,
        block: (GlVideoPlayerController) -> Unit
    ) {
        textureView?.let { textureView ->
            EGLOutput.executeOnPlayer(textureView, onlyForEditTexture) { block(it) }
        }
    }

    companion object {
        private val VIDEO_PREPARED_TIMEOUT = if (BuildConfig.DEBUG) 5000L else 3000L
        private const val TEXTURE_VIEW_TEMPORARY_SCALE = 1.001F
    }

}
