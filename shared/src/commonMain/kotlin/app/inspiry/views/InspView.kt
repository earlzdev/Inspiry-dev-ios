package app.inspiry.views

import app.inspiry.core.animator.helper.AbsAnimationHelper
import app.inspiry.core.animator.helper.CommonAnimationHelper
import app.inspiry.core.data.Rect
import app.inspiry.core.data.TouchAction
import app.inspiry.core.database.ExternalResourceDao
import app.inspiry.core.log.KLogger
import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.media.*
import app.inspiry.core.opengl.programPresets.ShaderType
import app.inspiry.core.opengl.programPresets.TemplateMask
import app.inspiry.core.opengl.programPresets.TextureMaskProvider
import app.inspiry.core.manager.DurationCalculator
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.LayoutPosition
import app.inspiry.core.media.Media
import app.inspiry.core.media.MediaImage
import app.inspiry.core.opengl.programPresets.TextureMaskHelper
import app.inspiry.core.util.ArgbColorManager
import app.inspiry.core.util.WorkerThread
import app.inspiry.edit.instruments.SelectionType
import app.inspiry.palette.model.AbsPaletteColor
import app.inspiry.palette.model.PaletteColor
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.views.group.InspGroupView
import app.inspiry.views.media.InspMediaView
import app.inspiry.views.path.InspPathView
import app.inspiry.views.simplevideo.InspSimpleVideoView
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.template.TemplateMode
import app.inspiry.views.text.InspTextView
import app.inspiry.views.touch.MovableTouchHelper
import app.inspiry.views.touch.MovableTouchHelperFactory
import app.inspiry.views.vector.InspVectorView
import app.inspiry.views.viewplatform.ViewPlatform
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.math.min
import kotlin.math.roundToInt

abstract class InspView<T : Media>(
    val media: T,
    var parentInsp: InspParent?,
    var view: ViewPlatform?,
    val unitsConverter: BaseUnitsConverter,
    var animationHelper: AbsAnimationHelper<*>?,
    loggerGetter: LoggerGetter,
    private val touchHelperFactory: MovableTouchHelperFactory,
    val templateParent: InspTemplateView
) {

    var attachedScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        private set

    open val logger: KLogger = loggerGetter.getLogger("InspView")

    val isInitialized = MutableStateFlow(false)
    private var hidden = false

    val templateMode: TemplateMode
        get() = templateParent.templateMode

    /**
     * including InspAnimator.startFrame, but not including media.startFrame
     */
    open var duration: Int = 0

    /**
     * includes startTime of this particular animator,
     * but doesn't include startTime of the object media
     */
    var durationIn: Int = 0

    /**
     * doesn't include startTime since startTime works differently for outAnimators
     */
    var durationOut: Int = 0

    var userScaleX: Float = 1f
    var userScaleY: Float = 1f

    open var currentFrame: Int = 0
        set(value) {
            val oldVal = field
            field = value
            onCurrentFrameChanged(value, oldVal)
        }

    private var _initialWidth: Int? = null
    val initialWidth: Int
        get() {
            _initialWidth?.let {
                return it
            }
            _initialWidth = view?.width
            return _initialWidth!!
        }

    private var _initialHeight: Int? = null
    val initialHeight: Int
        get() {
            _initialHeight?.let {
                return it
            }
            _initialHeight = view?.height
            return _initialHeight!!
        }

    private lateinit var _initialLayout: LayoutPosition

    val initialLayout: LayoutPosition
        get() {
            if (this::_initialLayout.isInitialized) return _initialLayout
            _initialLayout = LayoutPosition(
                media.layoutPosition.width,
                media.layoutPosition.height,
                media.layoutPosition.alignBy,
                media.layoutPosition.x,
                media.layoutPosition.y,
                media.layoutPosition.paddingEnd,
                media.layoutPosition.paddingBottom,
                media.layoutPosition.paddingStart,
                media.layoutPosition.paddingTop,
                media.layoutPosition.marginRight,
                media.layoutPosition.marginBottom,
                media.layoutPosition.marginLeft,
                media.layoutPosition.marginTop,
                media.layoutPosition.relativeToParent
            )
            return _initialLayout
        }

    val isLogo: Boolean
        get() = this is InspMediaView && media.isLogo

    fun paddingScale(scaleX: Float?, scaleY: Float?, unitsConverter: BaseUnitsConverter) {

        with(initialLayout) {

            if (paddingBottom != null) media.layoutPosition.paddingBottom =
                unitsConverter.unitsMultiply(paddingBottom!!, scaleY ?: userScaleY)

            if (paddingTop != null) media.layoutPosition.paddingTop =
                unitsConverter.unitsMultiply(paddingTop!!, scaleY ?: userScaleY)

            if (paddingStart != null) media.layoutPosition.paddingStart =
                unitsConverter.unitsMultiply(paddingStart!!, scaleX ?: userScaleX)

            if (paddingEnd != null) media.layoutPosition.paddingEnd =
                unitsConverter.unitsMultiply(paddingEnd!!, scaleX ?: userScaleX)
        }
    }

    protected fun nullifyInitialSize() {
        _initialHeight = null
        _initialWidth = null
    }

    fun syncScaleWithMedia(linkedView: InspView<*>) {
        val scale =
            maxOf(linkedView.userScaleX, linkedView.userScaleY * SOCIAL_ICON_AUTO_SIZE_RATIO)
        _initialWidth = (viewWidth / scale).roundToInt()
        _initialHeight = (viewHeight / scale).roundToInt()
        userScaleX = scale
        userScaleY = scale
    }

    open fun getDefaultSource(): String? = null

    protected open fun onCurrentFrameChanged(newVal: Int, oldVal: Int) {
        animationHelper?.preDrawAnimations(newVal)
        view?.invalidate()
    }

    /**
     * TODO: can hide after is a little bit dumb. Need to think about it.
     */
    inline fun onCurrentFrameChangedViewThatCanHide(
        value: Int,
        canHideAfter: Boolean,
        changeField: (Int) -> Unit
    ) {
        var newValue = value

        val templateParent = templateParentNullable

        val alwaysVisible = templateParent?.textViewsAlwaysVisible == true
        if (alwaysVisible) {

            val staticFrameForEdit = getStaticFrameForEdit()
            if (staticFrameForEdit != null)
                newValue = staticFrameForEdit
            showView()

        } else {
            val startFrame = getStartFrameShortCut()

            if (startFrame > newValue || (canHideAfter && newValue > startFrame + duration)) {
                hideView()
            } else {
                showView()
            }
        }

        changeField(newValue)

        animationHelper?.preDrawAnimations(newValue)
        if (alwaysVisible)
            setNewAlpha(1f)
        view?.invalidate()
    }

    var radius: Float = 0f
    var mClipBounds: Rect? = null

    var movableTouchHelper: MovableTouchHelper? = null

    fun createMovableTouch() {
        val isSocial = media.isSocialImageOrVector()

        movableTouchHelper = if ((isSocial || media.isMovable == true)
            && templateMode == TemplateMode.EDIT
        ) touchHelperFactory.create(this)
        else null
    }

    open fun onSelectedChange(isSelected: Boolean) {

    }


    init {
        view?.onSizeChangeListener = ::onSizeChanged
        view?.onAttachListener = ::onAttach
        view?.onDetachListener = ::onDetach
        createMovableTouch()
    }

    open fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        if (oldW != 0 && oldH != 0)
            refreshClipBoundsSize()
    }

    var isAttached: Boolean = false

    open fun onAttach() {
        isAttached = true
        if (!attachedScope.isActive) {
            attachedScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        }
    }

    open fun onDetach() {
        attachedScope.cancel(message = "onDetachedFromWindow")
        isAttached = false
    }

    private fun needToRefreshClipBoundsSize() =
        mClipBounds != null || animationHelper?.hasClipPath() == true

    val viewWidth: Int
        get() = view?.width ?: 0
    val viewHeight: Int
        get() = view?.height ?: 0

    val paddingLeft: Int
        get() = view?.paddingLeft ?: 0

    val paddingRight: Int
        get() = view?.paddingRight ?: 0

    val paddingTop: Int
        get() = view?.paddingTop ?: 0

    val paddingBottom: Int
        get() = view?.paddingBottom ?: 0

    protected val parentWidth: Int
        get() = if (media.layoutPosition.relativeToParent)
            parentInsp?.viewWidth ?: 0
        else {
            templateParent.viewWidth
        }

    protected val parentHeight: Int
        get() =
            if (media.layoutPosition.relativeToParent)
                parentInsp?.viewHeight ?: 0
            else {
                templateParent.viewHeight
            }

    // when the size is changed, clipBounds still have the same size.
    // so this should be called in View::onSizeChanged
    fun refreshClipBoundsSize() {
        if (needToRefreshClipBoundsSize()) {
            animationHelper?.onSizeChanged()
        }
    }

    fun getStartFrameShortCut() = if (media.startFrame == -1) {
        templateParent.getDuration() - duration
    } else
        media.startFrame


    fun setCornerRadius(radius: Float) {
        if (radius != this.radius) {
            this.radius = radius
            setInnerCornerRadius(radius)
        }
    }

    fun findRootParentNotTemplate(view: InspView<*>): InspGroupView? {
        if (view.parentInsp is InspGroupView) {
            return findRootParentNotTemplate(view.parentInsp as? InspGroupView ?: return null)
        } else if (view is InspGroupView) {
            return view
        } else {
            return null
        }
    }

    fun parentDependsOnThisView(): Boolean {
        return media.dependsOnParent && parentInsp is InspGroupView
    }

    fun parentDependsOnThisViewNotLinked(): Boolean {
        return media.dependsOnParent && parentInsp is InspGroupView && (parentInsp as? InspGroupView)?.media?.isGroupLinked() == false
    }

    fun findParentIfDependsOnIt(): InspGroupView? {
        if (parentDependsOnThisView()) {
            return parentInsp as InspGroupView
        }
        return null
    }

    fun findParentIfDependsOnItNotLinked(): InspGroupView? {
        if (parentDependsOnThisViewNotLinked()) {
            return parentInsp as InspGroupView
        }
        return null
    }

    fun isSocialIcon() = media.isSocialImageOrVector()

    fun invalidateParentIfTexture(delay: Long = 0L, instantly: Boolean = false) {
        if (media.textureIndex != null && templateMode == TemplateMode.EDIT) {
            (parentInsp as? InspGroupView?)?.invalidateRedrawProgram(delay, instantly)
        }
    }

    fun setupRotation(rotationAngleDegree: Float) {
        setRotationConsiderParent(rotationAngleDegree)
        templateParent.isChanged.value = true
        invalidateParentIfTexture()
    }

    fun userChangesRotation(angle: Float) {
        templateParent.isChanged.value = true
        media.rotation = angle - (animationHelper?.animationRotation ?: 0f)
        updateRotation()
    }

    fun updateRotation() {
        view?.rotation = getRealRotation()
    }

    fun getRealRotation() =
        media.rotation + (animationHelper?.animationRotation ?: 0f)

    fun applyScaleX(): Int {
        val newWidth = initialWidth * userScaleX
        setNewWidth(newWidth)
        return newWidth.roundToInt()
    }

    fun applyScaleY(): Int {
        val newHeight = initialHeight * userScaleY
        setNewHeight(newHeight)
        return newHeight.roundToInt()
    }

    open fun hideView() {
        hidden = true
        view?.translationX = getRealTranslationX()
    }

    /*
     We don't use Androidview?.visibility for opengl sake. Because some textureView callbacks don't work if it is invisible.

     */
    open fun showView() {
        hidden = false
        view?.translationX = getRealTranslationX()
    }

    open fun isDuplicate() = false
    open fun maySwitchToAutosizeMode() {}


    fun setNewElevation(elevation: Float) {
        animationHelper?.notifyViewElevationChanged()
        view?.setElevation(elevation)
    }

    open fun setInnerCornerRadius(radius: Float) {
        animationHelper?.notifyViewCornerRadiusChanged()
    }

    open fun mayBeInvisible(): Boolean = true

    open fun setPadding(
        layoutPosition: LayoutPosition,
        parentWidth: Int,
        parentHeight: Int,
        unitsConverter: BaseUnitsConverter
    ) {
        view?.setPadding(layoutPosition, parentWidth, parentHeight, unitsConverter)
    }

    open fun setMargin(
        layoutPosition: LayoutPosition,
        parentWidth: Int,
        parentHeight: Int,
        unitsConverter: BaseUnitsConverter
    ) {
        view?.setMargin(layoutPosition, parentWidth, parentHeight, unitsConverter)
    }

    open fun getStaticFrameForEdit(): Int? {
        return durationIn + getStartFrameShortCut()
    }

    @WorkerThread
    open fun prepareAnimation(frame: Int) {

    }

    open fun calcDurations() {
        durationIn = DurationCalculator.calcDurationIn(media.animatorsIn)
        durationOut = DurationCalculator.calcDurationOut(media.animatorsOut)

        //otherwise take duration by shortcut - afterCalcDurations
        if (media.minDuration >= 0) {
            duration = getMinPossibleDuration(true)
        }
    }

    open fun getMinPossibleDuration(includeDelayBeforeEnd: Boolean): Int {
        return DurationCalculator.getMinPossibleDuration(
            media,
            durationIn,
            durationOut,
            includeDelayBeforeEnd
        )
    }

    fun resolveMinDurationShortcut(): Int? {
        val templateParent = templateParentNullable

        return when {
            templateParent == null -> {
                null
            }
            media.minDuration == Media.MIN_DURATION_AS_TEMPLATE -> {
                templateParent.maxFrames - media.startFrameRemoveShortcut()

            }
            media.minDuration < 0 -> {
                templateParent.maxFrames - media.startFrameRemoveShortcut() + media.minDuration
            }
            else -> null
        }
    }

    open fun afterCalcDurations(durationIn: Int, durationOut: Int, durationTotal: Int) {

        val minDurationShortcut = resolveMinDurationShortcut()
        if (minDurationShortcut != null)
            duration = minDurationShortcut
    }

    fun getParentGroupOrThis(): InspView<*> {
        val parent = findParentIfDependsOnIt()
        return parent ?: this
    }

    open fun onTemplateSizeChanged(width: Int, height: Int) {
        view?.translationX =
            getRealTranslationX()
        view?.translationY =
            getRealTranslationY()
    }


    fun getCornerRadiusAbsolute() = radius * min(viewHeight, viewWidth) / 2.0f

    private fun applyTranslationXForDuplicates() {
        templateParent.allTextViews.filter {
            it.isDuplicate() && it.media.duplicate == media.id
        }.forEach {
            it.media.setNewTranslationX(media.translationX)
            it.updateTranslationX(false)
        }
    }

    private fun applyTranslationYForDuplicates() {
        templateParent.allTextViews.filter {
            it.isDuplicate() && it.media.duplicate == media.id
        }.forEach {
            it.media.setNewTranslationY(media.translationY)
            it.updateTranslationY(false)
        }
    }

    fun incrementTranslationX(deltaX: Float) {

        val view = findParentIfDependsOnIt() ?: this

        view.media.setNewTranslationX(view.media.translationX + deltaX)
        view.updateTranslationX(true)
        applyTranslationXForDuplicates()
    }

    fun incrementTranslationY(deltaY: Float) {

        val view = findParentIfDependsOnIt() ?: this

        view.media.setNewTranslationY(view.media.translationY + deltaY)
        view.updateTranslationY(true)
        applyTranslationYForDuplicates()
    }

    fun updateTranslationX(changedElseAnimated: Boolean) {
        view?.translationX = getRealTranslationX()
        notifyTranslationChanged(changedElseAnimated)
    }

    fun updateTranslationY(changedElseAnimated: Boolean) {
        view?.translationY = getRealTranslationY()
        notifyTranslationChanged(changedElseAnimated)
    }

    fun getRealTranslationY() =
        (media.translationY + (animationHelper?.animationTranslationY ?: 0f)) * templateParent.viewHeight

    fun getRealTranslationX() = if (hidden) 10000f else
        (media.translationX + (animationHelper?.animationTranslationX ?: 0f)) * templateParent.viewWidth

    private fun notifyTranslationChanged(changedElseAnimated: Boolean) {
        val templateParent = templateParent
        if (changedElseAnimated) {
            templateParent.isChanged.value = true
            invalidateParentIfTexture()
        }

        templateParent.onInsideViewMoved(this, changedElseAnimated)
    }

    protected fun setRotationConsiderParent(rotation: Float) {
        if (parentDependsOnThisViewNotLinked()) {
            val parentGroup = parentInsp as InspGroupView
            parentGroup.userChangesRotation(rotation)
            view?.invalidateRotationParentChanged()
        } else {
            userChangesRotation(rotation)
        }
    }

    open fun setNewBackgroundColor(color: Int) {
        media.backgroundGradient = null
        media.backgroundColor = color
        refreshBackgroundColor()
    }

    open fun setNewBackgroundGradient(gradient: PaletteLinearGradient) {
        media.backgroundGradient = gradient
        view?.setBackground(gradient)
    }

    open fun removeFromActionButton(externalResourceDao: ExternalResourceDao) {
        removeThisView()
    }

    fun removeThisView() {
        templateParent.isChanged.value = true

        val parent = findParentIfDependsOnItNotLinked()
        if (parent != null) {
            templateParent.removeInspView(parent)
        } else {
            templateParent.removeInspView(this)
        }
    }

    private var jobTemplateSizeChange: Job? = null
    open fun refresh() {
        animationHelper?.resetLastTimeTriggeredEnd()
        refreshBackgroundColor()
        view?.rotation = media.rotation

        jobTemplateSizeChange?.cancel()
        jobTemplateSizeChange = attachedScope.launch {
            templateParent.currentSize.collect {
                if (it != null) {
                    onTemplateSizeChanged(it.width, it.height)
                }
            }
        }
    }


    fun scalePadding(scaleX: Float? = null, scaleY: Float? = null) {
        paddingScale(scaleX = scaleX, scaleY = scaleY, unitsConverter = unitsConverter)
        setPadding(
            media.layoutPosition,
            parentWidth = parentWidth,
            parentHeight = parentHeight,
            unitsConverter = unitsConverter
        )
    }

    open fun scaleMargin(scaleX: Float? = null, scaleY: Float? = null) {

        //social icon margins will scale with parent group
        if (media.isSocialImageOrVector() && scaleX == null && scaleY == null) return

        with(initialLayout) {

            if (marginBottom != null) media.layoutPosition.marginBottom =
                unitsConverter.unitsMultiply(marginBottom!!, scaleY ?: userScaleY)

            if (marginTop != null) media.layoutPosition.marginTop =
                unitsConverter.unitsMultiply(marginTop!!, scaleY ?: userScaleY)

            if (marginLeft != null) media.layoutPosition.marginLeft =
                unitsConverter.unitsMultiply(marginLeft!!, scaleX ?: userScaleX)

            if (marginRight != null) media.layoutPosition.marginRight =
                unitsConverter.unitsMultiply(marginRight!!, scaleX ?: userScaleX)
        }

        setMargin(
            media.layoutPosition,
            parentWidth = parentWidth,
            parentHeight = parentHeight,
            unitsConverter = unitsConverter
        )
    }

    protected fun setNewWidth(value: Float) {

        media.layoutPosition.width = "${value / parentWidth}w"
    }

    protected fun setNewHeight(value: Float) {
        media.layoutPosition.height = "${value / parentHeight}h"
    }

    open fun refreshBackgroundColor() {
        view?.setBackground(media)
    }

    //to avoid improperly layout calls. doesn't work thou
    open fun preRefresh() {

    }

    open fun setNewAlpha(alpha: Float) {
        view?.setAlpha(alpha)
    }

    fun setBackgroundColorFromAnimation(color: Int) {
        view?.setBackgroundColor(color)
    }

    val isSelectedForEdit: Boolean
        get() = templateParent.selectedView == this

    fun templateBackgroundChanged() {
        view?.invalidate()
    }

    // TODO: maybe make null if we have detached from parent -> this view was removed.
    val templateParentNullable: InspTemplateView?
        get() = templateParent


    private var animatedBackground: AbsPaletteColor = PaletteColor(0)

    fun updateBackgroundForAnimation() {
        animatedBackground = media.backgroundGradient ?: PaletteColor(media.backgroundColor)
    }

    fun setBackgroundAlphaForAnimation(value: Float) {
        updateBackgroundForAnimation()
        if (animatedBackground is PaletteLinearGradient) {
            val userAlpha = ArgbColorManager.alphaDegree(animatedBackground.getFirstColor())
            animatedBackground =
                animatedBackground.getWithAlpha((value * 255 * userAlpha).roundToInt())
            view?.setBackground(animatedBackground as PaletteLinearGradient)
        } else {
            val color = animatedBackground.getFirstColor()
            val colorAlpha = ArgbColorManager.alphaDegree(color) * value
            setBackgroundColorFromAnimation(ArgbColorManager.applyAlphaToColor(color, colorAlpha))
        }
        view?.invalidate()
    }

    open fun setUserRadius(value: Float) {
        radius = value
    }

    open fun getUserRadius(): Float = radius

    /**
     *  when isColorFilterDisabled=true for MediaImage - changing color for MediaImage will be unavailable
     *  when colorChangeDisabled=true for ANY movable Media - color change will be unavailable for it
     */
    fun isColorChangeDisabled(): Boolean {
        val colorFilterDisabled = if (media is MediaImage) media.isColorFilterDisabled() else false
        return colorFilterDisabled || media.colorChangeDisabled
    }

    open fun colorLayerCount() = DEFAULT_COLOR_LAYERS
    open fun gradientslayerCount() = 0

    abstract fun rememberInitialColors()
    abstract fun restoreInitialColors(layer: Int, isBack: Boolean)

    open fun onTemplateModeHasChanged(newMode: TemplateMode) {
        createMovableTouch()
    }

    fun setSelected() {
        templateParent.changeSelectedView(value = this)
    }
    fun asInspSimpleVideoView(): InspSimpleVideoView? {
        return this as? InspSimpleVideoView
    }
    fun asInspTextView(): InspTextView? {
        return this as? InspTextView
    }
    fun asInspGroupView(): InspGroupView? {
        return this as? InspGroupView
    }

    fun asInspMediaView(): InspMediaView? {
        return this as? InspMediaView
    }

    fun asInspVectorView(): InspVectorView? {
        return this as? InspVectorView
    }

    fun asInspPathView(): InspPathView? {
        return this as? InspPathView
    }

    fun asInspView(): InspGroupView? {
        return this as? InspGroupView
    }

    fun touchesEnabled(): Boolean {
        return templateParent.templateMode == TemplateMode.EDIT && (media.isMovable == true || (media as? MediaImage)?.isEditable == true)
    }

    fun isInSlides(): Boolean {
        var parent = parentInsp
        do {
            if (parent is InspGroupView) {
                if (parent.media.slides != null)
                    return true
                else {
                    parent = parent.parentInsp
                }
            } else {
                return false
            }
        } while (true)
    }

    fun asGeneric(): InspView<*> {
        return this
    }

    companion object {
        //the scaling of social icon depends on scaling of the text
        //this constant controls the dependence of scale of icon on vertical scale of the text
        const val SOCIAL_ICON_AUTO_SIZE_RATIO = 0.5f
        const val DEFAULT_COLOR_LAYERS = 1

        fun getDefaultMovableTouchActions() =
            mutableListOf(
                TouchAction.button_scale,
                TouchAction.button_rotate,
                TouchAction.button_close,
                TouchAction.button_duplicate,
                TouchAction.move
            )

        fun getDefaultEditableTouchActions() =
            mutableListOf(
                TouchAction.button_close
            )
    }

    fun getType(): SelectionType? {
        return when (this) {
            is InspSimpleVideoView -> SelectionType.VIDEO_DEMO
            is InspMediaView -> SelectionType.IMAGE_OR_VIDEO
            is InspVectorView -> SelectionType.VECTOR
            is InspPathView -> SelectionType.PATH
            is InspGroupView -> SelectionType.GROUP
            is InspTextView -> SelectionType.TEXT
            else -> null
        }
    }

    /**
     * getting the boundaries of the view relative to the template
     */
    fun offsetViewBounds(rect: Rect, recursive: Boolean) {
        val dx = view?.x ?: 0f
        val dy = view?.y ?: 0f
        rect.offset(dx.roundToInt(), dy.roundToInt())
        if (parentInsp is InspGroupView && recursive)
                (parentInsp as? InspGroupView)?.offsetViewBounds(rect, true)
    }

    open fun getViewBounds(recursive: Boolean = true): Rect {
        val rect = Rect(
            0,
            0,
            viewWidth,
            viewHeight
        )
        offsetViewBounds(rect, recursive)
        return rect
    }

    fun hasTextureMask(): Boolean {
        val templateMask = getTemplateMaskOrNull()
        return if (templateMask != null) !TextureMaskHelper.IOS_UNSUPPORTED_SHADER_TYPES.contains(templateMask.shaderType ?: ShaderType.COMMON_MASK)
        else false
    }
    fun getTemplateMaskOrNull(): TemplateMask?
    {
        return  (media as? MediaGroup)?.templateMask ?: (media as? MediaImage)?.templateMask
    }

    fun getMaskView(): InspView<*>? {
        if (this is InspMediaView && media.templateMask?.shaderType == ShaderType.COMMON_MASK) {
            return templateParent.allViews.find { it.media.id == media.templateMask?.texturesID?.firstOrNull() }
        }

        if (this is InspGroupView && media.templateMask?.shaderType == ShaderType.COMMON_MASK) {
            return templateParent.allViews.find { it.media.id == media.templateMask?.texturesID?.firstOrNull() }
        }
        return null
    }

    /**
     * created for ios
     * we need to remove inner views for removing strong references
     * because memory is not released while the strong reference is live
     */
    open fun releaseInner() {
        media.view = null
        parentInsp = null
        (animationHelper as CommonAnimationHelper).inspView = null
        parentInsp = null
        view = null
        animationHelper = null
    }

    val id: String?
        get() = media.id

    fun getAbsoluteRotation(): Float {
        var rotation = getRealRotation()
        if (parentInsp is InspGroupView) rotation += (parentInsp as? InspGroupView)?.getAbsoluteRotation() ?: 0f
        return rotation
    }
}


fun <T, R : Comparable<R>> Iterable<T>.maxByReturnMax(selector: (T) -> R): R? {
    val iterator = iterator()
    if (!iterator.hasNext()) return null
    val maxElem = iterator.next()
    var maxValue = selector(maxElem)
    if (!iterator.hasNext()) return maxValue
    do {
        val e = iterator.next()
        val v = selector(e)
        if (maxValue < v) {
            maxValue = v
        }
    } while (iterator.hasNext())
    return maxValue
}
