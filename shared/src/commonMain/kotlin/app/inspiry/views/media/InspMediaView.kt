package app.inspiry.views.media

import app.inspiry.core.animator.InspAnimator
import app.inspiry.core.animator.appliers.ScaleInnerAnimApplier
import app.inspiry.core.animator.appliers.ScaleOuterAnimApplier
import app.inspiry.core.animator.clipmask.shape.ShapeType
import app.inspiry.core.animator.helper.AbsAnimationHelper
import app.inspiry.core.data.*
import app.inspiry.core.database.ExternalResourceDao
import app.inspiry.core.log.GlobalLogger
import app.inspiry.core.log.KLogger
import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.media.*
import app.inspiry.core.media.Media.Companion.MIN_DURATION_AS_TEMPLATE
import app.inspiry.core.util.*
import app.inspiry.removebg.RemoveBgFileManager
import app.inspiry.views.InspParent
import app.inspiry.views.InspView
import app.inspiry.views.group.InspGroupView
import app.inspiry.views.slides.SlidesParent
import app.inspiry.views.template.*
import app.inspiry.views.touch.MovableTouchHelperFactory
import app.inspiry.views.viewplatform.ViewPlatform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.math.min

class InspMediaView(
    media: MediaImage,
    parentInsp: InspParent?,
    view: ViewPlatform?,
    unitsConverter: BaseUnitsConverter,
    animationHelper: AbsAnimationHelper<*>?,
    loggerGetter: LoggerGetter,
    var innerMediaView: InnerMediaView?, touchHelperFactory: MovableTouchHelperFactory,
    templateParent: InspTemplateView,
    val fileSystem: FileSystem
) : InspView<MediaImage>(
    media, parentInsp, view, unitsConverter, animationHelper, loggerGetter,
    touchHelperFactory,
    templateParent
) {

    /**
     * todo:  We need to show media only after StartTime and hide after Duration, but it does not work right yet
     * fixme: it is necessary to check and fix all the templates, or use the default duration "as_template"
     */

//    override var currentFrame: Int = 0
//        set(value) {
//                onCurrentFrameChangedViewThatCanHide(value, canHideAfter = false) { newValue ->
//                    field = newValue
//                }
//        }

    val shapeState = MutableStateFlow(media.shape)

    val sourceCount: Int
        get() = media.programCreator?.getEditableTexturesCount() ?: 1

    override val logger: KLogger = loggerGetter.getLogger("media-view")

    override fun mayBeInvisible(): Boolean = !media.hasProgramOrVideo()

    override fun getDefaultSource() = media.defaultSource

    override fun isDuplicate() = media.duplicate != null

    fun hasVideo() = media.hasVideo()

    fun isVideo() = media.isVideo && media.originalSource != null

    fun setInnerImageScale(scaleX: Float, scaleY: Float) {
        innerMediaView?.setInnerImageScale(scaleX, scaleY)
    }

    //to remove medias, added on preview
    var clearMediaOnEdit = false

    fun setBlurRadius(blurRadius: Float, async: Boolean) {
        // don't apply blur in async mode while recording, because this func will also be called with async = false params.
        if (isNotPickImage() && !(templateParent.recordMode == RecordMode.VIDEO && async))
            innerMediaView?.setBlurRadius(blurRadius, async)
    }

    override fun releaseInner() {
        innerMediaView = null
        super.releaseInner()
    }

    private fun undoRemoveBgView(undoRemoveBgData: UndoRemoveBgData) {
        media.apply {
            this.undoRemoveBgData = null
            colorFilterMode = undoRemoveBgData.colorFilterMode
            borderWidth = undoRemoveBgData.borderWidth
            isMovable = undoRemoveBgData.isMovable
            userScaleY = 1f
            userScaleX = 1f
            touchActions = getDefaultEditableTouchActions()
            rotation = undoRemoveBgData.rotation
            keepAspect = false
            layoutPosition.width = undoRemoveBgData.width
            layoutPosition.height = undoRemoveBgData.height
            this.setNewTranslationX(undoRemoveBgData.translationX)
            this.setNewTranslationY(undoRemoveBgData.translationY)
            animatorsIn = undoRemoveBgData.animatorsIn
            animatorsOut = undoRemoveBgData.animatorsOut
            animatorsAll = undoRemoveBgData.animatorsAll
        }

        val newWidth = unitsConverter.convertUnitToPixelsF(
            undoRemoveBgData.width,
            parentWidth, parentHeight, forHorizontal = true
        )
        val newHeight = unitsConverter.convertUnitToPixelsF(
            undoRemoveBgData.height,
            parentWidth, parentHeight, forHorizontal = false
        )

        // scale animators may have changed.
        view?.scaleX = 1f
        view?.scaleY = 1f

        updateRotation()
        updateTranslationX(false)
        updateTranslationY(false)
        animationHelper?.preDrawAnimations(currentFrame)

        nullifyInitialSize()
        view?.changeSize(newWidth, newHeight)
        createMovableTouch()


        templateParent.isChanged.value = true
        invalidateParentIfTexture()
    }

    fun setNewShape(shape: ShapeType?, forDuplicated: Boolean = true) {
        GlobalLogger.debug("inspMediaView") {"set new shape for ${media.id}) is duplicate ${isDuplicate()}"}
        media.shape = if (shape == ShapeType.NOTHING) null else shape
        animationHelper?.clipMaskSettings?.shape = shape
        animationHelper?.shapeTransform()
        view?.invalidate()
        invalidateParentIfTexture()
        shapeState.value = shape
        templateParent.isChanged.value = true
        if (forDuplicated) doForDuplicated { it.setNewShape(shape, false) }
    }

    fun turnToRemovedBgView(newImageSize: Size) {

        val undoRemoveBgData = media.undoRemoveBgData ?: UndoRemoveBgData(
            colorFilterMode = media.colorFilterMode,
            borderWidth = media.borderWidth,
            isMovable = media.isMovable,
            width = media.layoutPosition.width,
            height = media.layoutPosition.height,
            translationX = media.translationX,
            translationY = media.translationY,
            rotation = media.rotation,
            animatorsIn = media.animatorsIn,
            animatorsOut = media.animatorsOut,
            animatorsAll = media.animatorsAll
        )

        // inner animations work very bad for movable stuff
        fun List<InspAnimator>.checkRemoveOrTurnToScaleOuter(): List<InspAnimator> {

            val hasOuterAnimation = this.any { it.animationApplier is ScaleOuterAnimApplier }
            return if (hasOuterAnimation) {
                filter { it.animationApplier !is ScaleInnerAnimApplier }

            } else {
                map { anim ->

                    if (anim.animationApplier is ScaleInnerAnimApplier) {

                        InspAnimator(
                            anim.startFrame, anim.duration,
                            anim.interpolator, ScaleOuterAnimApplier(
                                anim.animationApplier.from, anim.animationApplier.to,
                                anim.animationApplier.from, anim.animationApplier.to
                            )
                        )
                    } else {
                        anim
                    }
                }
            }
        }

        media.apply {
            this.undoRemoveBgData = undoRemoveBgData
            isMovable = true
            innerImageOffsetX = 0f
            innerImageOffsetY = 0f
            innerImageScale = 1f
            innerImageRotation = 0f
            userScaleY = 1f
            userScaleX = 1f
            borderWidth = null
            touchActions = getDefaultMovableTouchActions()
            keepAspect = true

            if (layoutPosition.width == LayoutPosition.MATCH_PARENT)
                layoutPosition.width = "1w"
            if (layoutPosition.height == LayoutPosition.MATCH_PARENT)
                layoutPosition.height = "1h"
            if (layoutPosition.width == LayoutPosition.WRAP_CONTENT)
                layoutPosition.width =
                    templateParent.currentSize.value?.width?.let { (viewWidth / it.toFloat()).toString() + "w" }
                        ?: "0.2w"
            if (layoutPosition.height == LayoutPosition.WRAP_CONTENT)
                layoutPosition.height =
                    templateParent.currentSize.value?.height?.let { (viewHeight / it.toFloat()).toString() + "h" }
                        ?: "0.2h"

            animatorsAll = animatorsAll.checkRemoveOrTurnToScaleOuter()
            animatorsIn = animatorsIn.checkRemoveOrTurnToScaleOuter()
            animatorsOut = animatorsOut.checkRemoveOrTurnToScaleOuter()
        }

        val currentSize = SizeF(viewWidth.toFloat(), viewHeight.toFloat())
        val newAspectRatio = newImageSize.width / newImageSize.height.toFloat()

        val newSize = InspMathUtil.convertAspectRatio(
            newAspectRatio,
            currentSize,
            makeBigger = false
        )

        setNewSizeAdjustTranslation(newSize, currentSize)

        setInnerImageScale(1f, 1f)
        animationHelper?.prepareAnimation(currentFrame)
        createMovableTouch()
    }

    private fun setNewSizeAdjustTranslation(newSize: SizeF, currentSize: SizeF) {
        val diffInHeight = currentSize.height - newSize.height
        if (diffInHeight != 0f) {
            incrementTranslationY((diffInHeight / 2) / templateParent.viewHeight)
        }

        val diffInWidth = currentSize.width - newSize.width
        if (diffInWidth != 0f) {
            incrementTranslationX((diffInWidth / 2) / templateParent.viewWidth)
        }

        templateParent.guidelineManager.hideOnTouchCancel(templateParent, this)
        nullifyInitialSize()

        setNewHeight(newSize.height)
        setNewWidth(newSize.width)

        view?.changeSize(newSize.width, newSize.height)
    }


    fun getImageScaleType() =
        if (templateMode == TemplateMode.EDIT && media.originalSource == null && !media.hasProgramOrVideo())
            ScaleType.CENTER_INSIDE
        else if (media.scaleType != null) media.scaleType!!
        else if (media.isMovable == true) ScaleType.FIT_CENTER
        else if (!media.isEditable) ScaleType.CENTER_CROP
        else ScaleType.MATRIX

    //probably wrong logic
    private fun isNotPickImage() = !media.isEditable
            || templateMode != TemplateMode.EDIT || media.originalSource != null || media.isMovable == true

    override fun onCurrentFrameChanged(newVal: Int, oldVal: Int) {
        super.onCurrentFrameChanged(newVal, oldVal)
        if (oldVal != newVal && newVal == 0 && templateParentNullable?.isRecording == false) {
            innerMediaView?.restartVideoIfExists()
        }
        if (isVideo()) {
            val videoDurationFrames = getDurationForTrimmingMillis() / FRAME_IN_MILLIS
            if ( currentFrame >= videoDurationFrames + media.startFrame ) {
                pauseVideoIfExists()
            }
        }

    }

    fun getTemplate() = templateParent.template

    inline fun doForDuplicated(action: (InspMediaView) -> Unit) {
        val roleModel = findRoleModelMedia()
        if (roleModel != null) {
            action(roleModel)
            roleModel.findDuplicatedMedias().forEach {
                if (it !== this@InspMediaView)
                    action(it)
            }
        } else {
            findDuplicatedMedias().forEach {
                action(it)
            }
        }
    }

    private var jobTrimVideoOffset: Job? = null
    private fun subscribeTrimPosChanged() {

        jobTrimVideoOffset?.cancel()
        media.videoStartTimeMs?.let {

            jobTrimVideoOffset = attachedScope.launch {
                it.drop(1).collect { newVideoStartTimeMs ->

                    templateParent.isChanged.value = true

                    if (media.id == MEDIA_ID_BACKGROUND) {
                        templateParent.template.palette.backgroundVideoStartMs = newVideoStartTimeMs
                    }

                    innerMediaView?.updateVideoStartTime(
                        media.originalSource ?: media.demoSource!!,
                        SOURCE_TEXTURE_INDEX, newVideoStartTimeMs
                    )

                    doForDuplicated { it.media.videoStartTimeMs?.value = newVideoStartTimeMs }
                }
            }
        }
    }

    fun insertNewVideo(
        videoStartTimeMs: Int,
        originalUri: String,
        textureIndex: Int,
        isLoopEnabled: Boolean?,
        setForDuplicates: Boolean
    ) {


        if (media.originalSource == originalUri)
            return

        templateParent.isChanged.value = true

        if (setForDuplicates) {
            doForDuplicated {
                it.insertNewVideo(
                    videoStartTimeMs,
                    originalUri,
                    textureIndex,
                    isLoopEnabled,
                    false
                )
            }
        }
        GlobalLogger.debug("inspMediaView") {"insert new video is source ${textureIndex.isSourceTexture()} isVideo ${isVideo()}"}
        if (textureIndex.isSourceTexture()) {
            media.originalSource = originalUri
            jobTrimVideoOffset?.cancel()
            jobCollectVideoVolume?.cancel()
            media.videoStartTimeMs = MutableStateFlow(videoStartTimeMs)
             media.videoEndTimeMs = MutableStateFlow(videoStartTimeMs + getDurationForTrimmingMillis(maxDuration = true))

            subscribeTrimPosChanged()
            if (media.duplicate.isNullOrEmpty()) {
                media.videoVolume = MutableStateFlow(1f)
                subscribeVideoVolumeChange()
            } else {
                media.videoVolume = null
            }
            media.isVideo = true
            if (isLoopEnabled != null)
                media.isLoopEnabled = isLoopEnabled
        }

        setVideoInner(originalUri, textureIndex)
    }


    override fun afterCalcDurations(durationIn: Int, durationOut: Int, durationTotal: Int) {
        super.afterCalcDurations(durationIn, durationOut, durationTotal)
        logger.debug { "afterCalcDurations minDuration ${media.minDuration}" }

        innerMediaView?.setVideoTotalDurationMs(getDurationForTrimmingMillis())
    }

    private fun setVideoInner(uri: String, textureIndex: Int = SOURCE_TEXTURE_INDEX) {
        innerMediaView?.setVideoInner(uri, textureIndex)

        if (textureIndex.isSourceTexture()) {
            onNewImageLoaded()
            resetInnerTranslation()
        }
    }

    fun canTrimVideo() = getVideoDurationMs() > getDurationForTrimmingMillis()

    fun drawVideoFrameAsync(frame: Int, sequential: Boolean) {
        innerMediaView?.drawVideoFrameAsync(frame, sequential)
    }

    fun setRecording(value: Boolean) {
        innerMediaView?.setRecording(value)
    }

    @WorkerThread
    fun drawVideoFrameSync(frame: Int, sequential: Boolean) {
        innerMediaView?.drawVideoFrameSync(frame, sequential)
    }

    fun setTranslateInner(translationX: Float, translationY: Float) {
        innerMediaView?.setTranslateInner(translationX, translationY)
    }

    /**
     * In editMode we use default program without any animation and don't take into account startTime
     */
    fun getViewTimeOffsetUs() = (getStartFrameShortCut() * FRAME_IN_MILLIS * 1000).toLong()

    private fun setInitialImage(
        originalSource: String?,
        demoSource: String?,
        demoInnerImageOffsetX: Float,
        demoInnerImageOffsetY: Float,
        demoInnerImageScale: Float, maySelectAfter: Boolean
    ) {

        val strPath = if (originalSource != null) originalSource
        else {
            media.innerImageOffsetX = demoInnerImageOffsetX
            media.innerImageOffsetY = demoInnerImageOffsetY
            media.innerImageScale = demoInnerImageScale

            demoSource
        }

        innerMediaView?.setImageInitial(strPath, onError = {
            setPickImage()
            if (!media.hasProgram()) templateParentNullable?.childHasFinishedInitializing(this)

        }, onSuccess = {
            if (maySelectAfter) {
                onNewImageLoaded()
                if (!media.hasProgram()) templateParentNullable?.childHasFinishedInitializing(this)
            }

        })
    }

    private fun hasInitialImage() =
        (templateMode == TemplateMode.LIST_DEMO && media.demoSource != null) ||
                media.originalSource != null

    private fun setInitialMedia(media: MediaImage, maySelectAfter: Boolean) {
        if (media.isVideo) {
            setVideoInner(media.originalSource ?: media.demoSource!!)
        } else {
            setInitialImage(
                media.originalSource,
                media.demoSource,
                media.demoOffsetX,
                media.demoOffsetY,
                media.demoScale,
                maySelectAfter
            )
        }
    }

    fun setNewBorderColor(color: Int?) {
        media.borderColor = color
        innerMediaView?.updateBorder()
        view?.invalidate()
    }

    override fun onTemplateModeHasChanged(newMode: TemplateMode) {
        super.onTemplateModeHasChanged(newMode)
        if (newMode == TemplateMode.PREVIEW) {
            animationHelper?.preDrawAnimations(currentFrame)
            showView()
        }
        if (clearMediaOnEdit && newMode == TemplateMode.EDIT) {
            media.originalSource = null
            clearMediaOnEdit = false
        }
        if (clearMediaOnEdit && newMode == TemplateMode.PREVIEW) {
            innerMediaView?.setImageInitial(media.originalSource!!, {}, {})
        }
        if (media.originalSource.isNullOrEmpty() && newMode != TemplateMode.LIST_DEMO) {
            if (newMode == TemplateMode.EDIT) {
                setPickImage()
            } else {
                if (isVideo()) {
                    innerMediaView?.pauseVideoIfExists()
                    innerMediaView?.drawVideoFrameAsync(currentFrame, false)
                }
                innerMediaView?.setImageInitial(null, {}, {})
            }
            invalidateParentIfTexture()
        }
        if (newMode == TemplateMode.EDIT && isInSlides()) {
            if (getSlidesParent().getSlidesMedia().firstOrNull()?.id != media.id) {
                super.hideView()
                return
            }
        }
    }

    fun setDisplayMedias(maySelectAfter: Boolean) {
        val roleModel = findRoleModelMedia()

        //it will be set in controller image
        if (roleModel != null && roleModel.hasInitialImage()) {

            setInitialMedia(roleModel.media, maySelectAfter)

        } else if (hasInitialImage()) {
            setInitialMedia(media, maySelectAfter)

        } else {
            var isRenderRunning = false


            if (media.isEditable) {
                if (templateMode == TemplateMode.EDIT || templateMode == TemplateMode.LIST_DEMO)
                    setPickImage()

            } else {

                if (innerMediaView?.setDisplayVideo() == true)
                    isRenderRunning = true
            }

            if (!isRenderRunning) {
                // It's called after render prepare completed
                callFinishedInitializing()
            }
        }
    }

    override fun prepareAnimation(frame: Int) {
        animationHelper?.prepareAnimation(frame)
    }

    override fun setNewAlpha(alpha: Float) {
        super.setNewAlpha(alpha * media.alpha)
    }

    private var jobCollectVideoVolume: Job? = null
    private fun subscribeVideoVolumeChange() {
        jobCollectVideoVolume?.cancel()

        media.videoVolume?.let {
            jobCollectVideoVolume = attachedScope.launch {
                //drop initial value.
                it.drop(1).collect {
                    innerMediaView?.updateVideoVolume(it)
                    templateParent.isChanged.value = true
                }
            }
        }
    }

    fun getVideoVolumeConsiderDuplicate(): MutableStateFlow<Float>? {
        val roleModel = findRoleModelMedia()
        if (roleModel != null)
            return roleModel.media.videoVolume
        else
            return media.videoVolume
    }

    override fun refresh() {
        super.refresh()
        calcDurations()
        afterCalcDurations(durationIn, durationOut, duration)

        if (media.colorFilter != null || media.alpha != 1f) {
            setColorFilter(media.colorFilter, media.alpha)
        }
        innerMediaView?.refresh()

        setDisplayMedias(true)

        subscribeVideoVolumeChange()
        subscribeTrimPosChanged()
    }

    private fun callFinishedInitializing() {
        innerMediaView?.doWhenSizeIsKnown {
            templateParent.childHasFinishedInitializing(this)
        }
    }

    private fun onClickEditMedia() {
        if ((media.originalSource == null) || isSelectedForEdit) {
            if (!isSelectedForEdit) {
                templateParent.changeSelectedView(null)
            }
            //templateParent.editCoordinator!!.pickNewImage(if (media.id == MEDIA_ID_BACKGROUND) null else this)
            templateParent.editAction(
                TemplateEditIntent.PICK_IMAGE,
                if (media.id == MEDIA_ID_BACKGROUND) null else this
            )
        }
    }

    private fun removeThisMedia() {
        if (!media.isColorFilterDisabled()) {
            media.colorFilterMode = ColorFilterMode.MULTIPLY
            setColorFilter(null, null)
        }
        setPickImage()
        innerMediaView?.removeInnerMedia()
        media.originalSource = null
        jobCollectVideoVolume?.cancel()
        media.videoVolume = null
        jobTrimVideoOffset?.cancel()
        media.videoStartTimeMs = null
        if (media.id == MEDIA_ID_BACKGROUND) {
            templateParentNullable?.template?.palette?.let {
                it.backgroundImage = null
                it.backgroundVideoLooped = null
                it.backgroundVideoStartMs = null
            }
        }
        media.isVideo = isDemoVideo()
        invalidateParentIfTexture()
    }

    private fun removeInnerMedia() {

        removeThisMedia()
        doForDuplicated { it.removeThisMedia() }

        updateTranslationX(false)
        updateTranslationY(false)

        templateParent.isChanged.value = true
        templateParent.changeSelectedView(null)
    }

    private fun removeThisSlide() {
        templateParent.slideUtilities.moveSlides(getSlidesParent(), this.media.id!!, -1) {
            templateParent.setFrameForEdit()
        }
    }

    override fun removeFromActionButton(externalResourceDao: ExternalResourceDao) {
        if (isInSlides() && getSlidesParent().getSlidesCount() > 1) {
            removeThisSlide()
            return
        }

        media.originalSource?.let {
            val isSourceRemovingBg = RemoveBgFileManager.isRemovedBgFile(it)

            if (isSourceRemovingBg && media.undoRemoveBgData?.isMovable != true) {
                attachedScope.launch(Dispatchers.Default) {
                    val withoutScheme = it.removeScheme()
                    if (externalResourceDao.onRemoveResource(withoutScheme))
                        fileSystem.delete(withoutScheme.toPath(), mustExist = false)
                }
            }
        }

        media.undoRemoveBgData?.let { undoRemoveBgView(it) }

        if (media.isMovable == true)
            removeThisView()
        else
            removeInnerMedia()
    }

    override fun getViewBounds(recursive: Boolean): Rect {
        val rect = Rect(
            0,
            0,
            viewWidth + 0,
            viewHeight + 0
        )
        animationHelper?.maskProvider?.lastShapeBounds?.let {
            if (it.width() != 0 && it.height() != 0)
                rect.set(it)

        }
        offsetViewBounds(rect, recursive)
        return rect
    }

    private fun isDemoVideo() = media.demoSource?.endsWith(".mp4") == true

    private fun onNewImageLoaded() {

        val needSelectImage = media.originalSource != null
                && templateMode == TemplateMode.EDIT
                && media.id != MEDIA_ID_BACKGROUND
                && ((media.isEditable || (media.isMovable == true && templateParentNullable?.isInitialized?.value == true)
                && !media.isSocialIcon)
                && !(isInSlides() && getSlidesParent().getSlidesCount() > 1)
                )

        logger.info { "onNewImageLoaded ${needSelectImage} isEditable ${media.isEditable}" }

        if (needSelectImage) {
            val templateParent = templateParentNullable
            templateParent?.changeSelectedView(this)

            if (media.isMovable == true && templateParent?.isInitialized?.value == true)
                currentFrame = templateParent.currentFrame
        }
    }

    //in case we applied translation to placeholder because of clip animation
    private fun resetInnerTranslation() {
        setTranslateInner(0f, 0f)
        animationHelper?.preDrawAnimations(currentFrame)
    }

    fun setPickImage() {
        if (media.isEditable) {
            innerMediaView?.setPickImage(if (templateMode == TemplateMode.EDIT) ::onClickEditMedia else null)
        }
    }

    fun findDuplicatedMedias(): List<InspMediaView> =
        if (!media.id.isNullOrEmpty() && media.duplicate.isNullOrEmpty()) {
            templateParentNullable?.mediaViews?.filter { it.media.duplicate == media.id }
                ?: emptyList()
        } else emptyList()

    fun findRoleModelMedia(): InspMediaView? = if (media.duplicate.isNullOrEmpty()) null else
        templateParentNullable?.mediaViews?.find { it.media.id == media.duplicate }


    fun onNewImagePicked(
        path: String,
        selectAfterLoad: Boolean,
        selectForDuplicates: Boolean = true,
        textureIndex: Int = 0
    ) {

        //search for duplicated images:
        if (selectForDuplicates) {
            doForDuplicated { it.onNewImagePicked(path, false, false, textureIndex) }
        }

        if (textureIndex.isSourceTexture()) media.originalSource = path
        templateParentNullable?.isChanged?.value = true

        innerMediaView?.loadNewImage(path, textureIndex, onSuccess = {

            templateParentNullable?.childHasFinishedInitializing(this)
            resetInnerTranslation()

            if (selectAfterLoad) {
                onNewImageLoaded()
            }
        })
    }

    fun getSlideDuration(): Int {
        val slides = getSlidesParent().getSlidesViews()
        val index = slides.indexOf(this)
        if (index == slides.lastIndex) return this.duration
        val nextSlideIndex = index + 1
        val nextFrameIn: Int
        with(slides[nextSlideIndex]) {
            nextFrameIn = durationIn + media.startFrame
        }
        return nextFrameIn - media.startFrame
    }

    fun getDurationForTrimmingMillis(maxDuration: Boolean = false): Int {

        if (media.videoEndTimeMs?.value != null && !maxDuration) {
            return (media.videoEndTimeMs?.value ?: 0) - (media.videoStartTimeMs?.value ?: 0)
        }

        val template = templateParent
        var duration: Int
        if (media.animatorsOut.isEmpty() && !isInSlides()) { //todo test on android
            duration = template.getDuration()
            duration -= getStartFrameShortCut()

            if (media.minDuration < 0 && media.minDuration != MIN_DURATION_AS_TEMPLATE)
                duration += media.minDuration
        } else {
            if (!isInSlides()) duration = this.duration
            else {
                duration = getSlideDuration()
            }
        }
        val result = (duration * FRAME_IN_MILLIS).toInt()
        val videoDuration = getVideoDurationMs().toInt()

        return min(result, videoDuration)
    }

    fun resetBackgroundColor() {
        if (media.initialColors?.backgroundGradient != null) setNewBackgroundGradient(media.initialColors!!.backgroundGradient!!)
        else {
            media.initialColors?.backgroundColor?.let { setNewBackgroundColor(it) }
        }
    }

    fun setColorFilter(color: Int?, alpha: Float?) {

        if (color == null && alpha == null) media.resetColorFilter()
        else {
            media.alpha = alpha ?: 1f
            color?.let { media.colorFilter = it }
        }

        logger.debug { "setColorFilter ${color?.let { ArgbColorManager.colorToString(it) }}" }

        if (media.colorFilter == null || media.colorFilter == 0)
            innerMediaView?.setColorFilter(null)
        else
            innerMediaView?.setColorFilter(media.colorFilter?.let {
                ArgbColorManager.colorWithoutAlpha(
                    it
                )
            }
            )
        view?.setAlpha(media.alpha)
        invalidateParentIfTexture()
        animationHelper?.resetLastTimeTriggeredEnd()
        animationHelper?.preDrawAnimations(currentFrame)
    }

    fun interruptImageLoading() {
        innerMediaView?.interruptImageLoading()
    }

    fun restoreRenderingInList() {
        innerMediaView?.restoreRenderingInList()
    }

    fun restartVideoIfExists() {
        innerMediaView?.restartVideoIfExists()
    }

    fun playVideoIfExists(forcePlay: Boolean) {
        if (isInSlides()) showView() //because slide might be hidden
        innerMediaView?.playVideoIfExists(forcePlay)

    }

    fun onClickPlayPauseFromInstruments(play: Boolean) {
        if (play) {
            playVideoIfExists(true)
        } else {
            pauseVideoIfExists()
        }
    }

    fun pauseVideoIfExists() {
        innerMediaView?.pauseVideoIfExists()
    }

    fun setUpMatrix() {
        innerMediaView?.setUpMatrix()
    }

    fun getVideoDurationMs() = innerMediaView?.getVideoDurationMs() ?: 0

    fun isVideoHasAudio(considerRoleModel: Boolean): Boolean {

        return if (considerRoleModel) {

            val roleModel = findRoleModelMedia()
            (roleModel?.isVideoHasAudio(false) ?: innerMediaView?.isVideoHasAudio()) == true


        } else {
            if (media.duplicate.isNullOrEmpty())
                innerMediaView?.isVideoHasAudio() == true
            else
                false
        }
    }

    fun canRemoveBg(): Boolean {
        return media.undoRemoveBgData == null && !media.isVideo && media.id != MEDIA_ID_BACKGROUND
    }

    fun showOnTopForEdit() {
        if (!isInSlides()) return
        templateParent.doWhenInitializedOnce { //todo it doesn't work well on ios
        GlobalLogger.debug("slide") { "show on top ${media.id}" }
        getSlidesParent().inspChildren.filterIsInstance<InspMediaView>().forEach {
            if (it.media.id != media.id) {
                GlobalLogger.debug("slide") { "hide view ${it.media.id}" }
                it.hideView()
            }
        }

        var inDuration = 0
        media.animatorsIn.forEach {
            val maxFrame = it.startFrame + it.duration
            if (maxFrame > inDuration) inDuration = maxFrame
        }
        this.showView()
        //animationHelper?.resetLastTimeTriggeredEnd()
        //animationHelper?.preDrawAnimations(inDuration + media.startFrame)
        setNewAlpha(1f)
        }
    }

    companion object {
        const val SOURCE_TEXTURE_INDEX = 0
    }

    override fun rememberInitialColors() {
        if (media.initialColors == null) {
            media.initialColors = InitialMediaColors(
                colorFilter = media.colorFilter,
                backgroundGradient = media.backgroundGradient,
                backgroundColor = media.backgroundColor,
                borderColor = media.borderColor,
                alpha = media.alpha
            )
        }
    }

    override fun onSelectedChange(isSelected: Boolean) {
        if (!isInSlides()) return
        if (isSelected) {
            showOnTopForEdit()
        } else {
            parentInsp?.inspChildren?.forEach {
                it.showView()
            }
            //templateParent.setFrameForEdit()
        }

    }

    override fun showView() {
        if (templateMode != TemplateMode.EDIT || templateParent.isPlaying.value) {
            super.showView()
            return
        }
        if (!isInSlides()) {
            super.showView()
            return
        } else {
            if (templateParent.selectedView?.media?.id == this.media.id) {
                super.showView()
                return
            }
            if (getSlidesParent().getSlidesMedia().firstOrNull()?.id == media.id) {
                super.showView()
                return
            }

        }
    }

    override fun restoreInitialColors(layer: Int, isBack: Boolean) {
        if (isBack) resetBackgroundColor()
        else setColorFilter(null, null)
        media.initialColors?.borderColor?.let { setNewBorderColor(it) }
    }

    fun getSlidesParent(): SlidesParent {
        return parentInsp as? InspGroupView ?: throw IllegalStateException("slides not in group!")
    }
}

fun Int.isSourceTexture() = this == InspMediaView.SOURCE_TEXTURE_INDEX