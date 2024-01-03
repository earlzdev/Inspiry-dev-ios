package app.inspiry.views.vector

import app.inspiry.core.animator.helper.AbsAnimationHelper
import app.inspiry.core.log.GlobalLogger
import app.inspiry.core.log.KLogger
import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.MediaVector
import app.inspiry.core.util.ArgbColorManager
import app.inspiry.core.util.getFileNameWithParent
import app.inspiry.palette.model.MediaPalette
import app.inspiry.palette.model.PaletteColor
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.views.InspParent
import app.inspiry.views.InspView
import app.inspiry.views.group.InspGroupView
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.template.TemplateMode
import app.inspiry.views.touch.MovableTouchHelperFactory
import app.inspiry.views.viewplatform.ViewPlatform
import kotlin.math.max
import kotlin.math.roundToInt


/**
 * This class is used for vector (static - svg) and (animated - json) objects
 */

class InspVectorView(
    media: MediaVector,
    parentInsp: InspParent?,
    view: ViewPlatform?,
    unitsConverter: BaseUnitsConverter,
    animationHelper: AbsAnimationHelper<*>?,
    var innerVectorView: InnerVectorView?,
    private val viewFps: Int, loggerGetter: LoggerGetter, touchHelperFactory: MovableTouchHelperFactory,
    templateParent: InspTemplateView
) : InspView<MediaVector>(
    media, parentInsp, view, unitsConverter, animationHelper,
    loggerGetter, touchHelperFactory, templateParent
) {

    private var lottieFps: Float = viewFps.toFloat()
    private var lottieDurationFrames: Int = 0
    var isStaticVector: Boolean = false

    override val logger: KLogger =
        loggerGetter.getLogger("InspVectorView ${media.originalSource.getFileNameWithParent()}")

    override fun getStaticFrameForEdit(): Int? {
        if (lottieDurationFrames != 0 && media.staticFrameForEdit != null)
            return getLottieFrameForEdit() + getStartFrameShortCut()
        else
            return null
    }

    override var currentFrame: Int = 0
        set(value) {
            onCurrentFrameChangedViewThatCanHide(value, canHideAfter = false) { newValue ->
                field = newValue
                setLottieFrame(max(newValue - getStartFrameShortCut(), 0))
            }
        }

    override fun getDefaultSource() = media.defaultSource

    init {
        innerVectorView?.onFailedToInitialize = ::onFailedToInitialize
        innerVectorView?.onInitialized = ::onInitialized
    }

    private fun getLottieFrameForEdit(): Int {

        return if (media.staticFrameForEdit == MediaVector.STATIC_FRAME_FOR_EDIT_LAST)
            lottieDurationFrames
        else if (media.staticFrameForEdit == MediaVector.STATIC_FRAME_FOR_EDIT_MIDDLE)
            lottieDurationFrames / 2
        else
            media.staticFrameForEdit!!
    }

    private fun setLottieFrame(frame: Int) {
        if (isStaticVector) return

        var newFrame = frame
        if (media.isLoopEnabled == true && lottieDurationFrames != 0) {
            newFrame = frame % (lottieDurationFrames + (media.loopedAnimationInterval ?: 0))
        }

        val lottieFrame = (newFrame * lottieFps / viewFps).roundToInt()

        innerVectorView?.lottieFrame = lottieFrame
    }

    override fun getMinPossibleDuration(includeDelayBeforeEnd: Boolean): Int {
        val res = super.getMinPossibleDuration(includeDelayBeforeEnd)
        return max(
            res,
            lottieDurationFrames + if (includeDelayBeforeEnd) media.delayBeforeEnd else 0
        )
    }

    fun getColorForLayer(layer: Int): Int? {
        if (media.mediaPalette.choices.size == 0) return media.mediaPalette.mainColor?.getFirstColor() ?: 0
        return media.mediaPalette.choices[layer].color
    }

    fun setColorForLayer(layerId: String, color: Int?) {
        media.mediaPalette.choices.find { it.elements.any { it == layerId } }?.color = color
        if (color == null) innerVectorView?.resetColorKeyPath(layerId, "**")
        else innerVectorView?.setColorKeyPath(color, layerId, "**")
    }

    override fun refresh() {
        super.refresh()


        if (media.originalSource.isEmpty()) {
            //don't need to load empty string
            innerVectorView?.clearDisplayResource()
            onInitialized(0f, 0)
            return
        }

        isStaticVector = media.originalSource.endsWith(".svg")
        if (media.scaleType != null) {
            innerVectorView?.setScaleType(media.scaleType)
        }

        logger.info { "refresh isStaticVector ${isStaticVector}" }


        if (isStaticVector) {
            innerVectorView?.loadSvg(media.originalSource)

        } else {
            innerVectorView?.loadAnimation(media.originalSource, media.isLottieAnimEnabled, media.blurEasing && templateParent.templateMode == TemplateMode.LIST_DEMO)
        }

        applyPalette(media.mediaPalette)
    }

    override fun setNewAlpha(alpha: Float) {
        super.setNewAlpha(alpha * media.mediaPalette.alpha)
    }

    private fun onFailedToInitialize(ignored: Throwable?) {
        templateParentNullable?.childHasFinishedInitializing(this)
    }

    fun onInitialized(lottieFps: Float, lottieDurationFrames: Int) {
        this.lottieFps = lottieFps
        this.lottieDurationFrames = lottieDurationFrames

        calcDurations()
        afterCalcDurations(durationIn, durationOut, duration)

        val templateParent = templateParentNullable ?: return

        if (templateParent.isInitialized.value) {
            currentFrame = templateParent.currentFrame
            if (media.isMovable == true)
                templateParent.changeSelectedView(this)
        }

        templateParent.childHasFinishedInitializing(this)
        if (isInSlides() && templateParent.templateMode == TemplateMode.EDIT) {
            val localDuration = lottieDurationFrames + getStartFrameShortCut()
            if (localDuration > templateParent.getDuration()) {
                templateParent.setNewDuration(localDuration)
            }
            getStaticFrameForEdit()?.let { currentFrame = it }
        }
        applyPalette(media.mediaPalette)
    }


    private fun setSingleColor(color: Int?) {
        if (isStaticVector) {
            if (color == null || color == 0)
                innerVectorView?.setColorFilter(color)
            else
                innerVectorView?.setColorFilter(ArgbColorManager.colorWithoutAlpha(color))

        } else {
            if (color == null || color == 0) {
                innerVectorView?.resetColorKeyPath("**")

            } else {
                innerVectorView?.setColorKeyPath(color, "**")
            }
        }
    }

    fun setAlpha(alpha: Float) {
        media.mediaPalette.alpha = alpha
        updateAlpha()
    }

    fun setColorFilter(color: Int?) {
        if (color == null) {
            media.mediaPalette.resetColors()
        } else {
            media.mediaPalette.mainColor = PaletteColor(color)
        }
        setSingleColor(color)
        updateAlpha()
    }


    fun updateAlpha() {
        view?.setAlpha(media.mediaPalette.alpha)
        // if we have alpha animations then we rewrite the value above.
        animationHelper?.preDrawAnimations(currentFrame)
    }

    fun applyPalette(newPalette: MediaPalette) {
        media.mediaPalette = newPalette

        if (newPalette.mainColor is PaletteLinearGradient) {
            innerVectorView?.setGradientKeyPath(
                newPalette.mainColor!!.getWithAlpha(255) as PaletteLinearGradient,
                "**"
            )

        } else if (newPalette.choices.isEmpty()) {

            setSingleColor(newPalette.mainColor?.getFirstColor())

        } else {

            newPalette.choices.forEach {
                val color = it.color?.let { ArgbColorManager.colorWithoutAlpha(it) }

                it.elements.forEach {

                    if (color == null)
                        innerVectorView?.resetColorKeyPath(it, "**")
                    else innerVectorView?.setColorKeyPath(color, it, "**")

                }
            }
        }
        updateAlpha()
    }

    override fun releaseInner() {
        innerVectorView = null
        super.releaseInner()
    }

    override fun colorLayerCount(): Int {
        val count = media.mediaPalette.choices.size
        return if (count == 0) 1 else count
    }

    fun setColorForLayer(layerID: Int, color: Int?) {
        if (media.mediaPalette.choices.size == 0) {
            setColorFilter(color)
            return
        }
        media.mediaPalette.choices[layerID].elements.forEach {
            setColorForLayer(it, color)
        }
    }

    override fun rememberInitialColors() {

    }

    /**
     * we don't know the initial colors for the vector,
     * but we can reset filter
     */
    override fun restoreInitialColors(layer: Int, isBack: Boolean) {
        if (layer < 0) setColorFilter(null)
        else setColorForLayer(layer, null)
    }
}

