package app.inspiry.views.path

import app.inspiry.core.animator.helper.AbsAnimationHelper
import app.inspiry.core.log.KLogger
import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.MediaPath
import app.inspiry.core.media.PaintStyle
import app.inspiry.core.media.getMovementsDuration
import app.inspiry.core.util.ArgbColorManager
import app.inspiry.core.util.PredefinedColors
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.views.InspParent
import app.inspiry.views.InspView
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.touch.MovableTouchHelperFactory
import app.inspiry.views.viewplatform.ViewPlatform
import kotlin.math.max


/**
 * Check PathMeasure class
 *
 * List of interesting text libraries:
 *
 * 1. https://github.com/totond/TextPathView
 * 2. https://github.com/dkmeteor/PathEffectTextView
 * 3. https://github.com/hanks-zyh/HTextView
 * 4. https://github.com/RomainPiel/Titanic
 */

class InspPathView(
    media: MediaPath, parentInsp: InspParent?, view: ViewPlatform?,
    unitsConverter: BaseUnitsConverter,
    animationHelper: AbsAnimationHelper<*>?,
    var path: CommonPath?,
    private var innerViewPath: InnerViewPath<*>?,
    loggerGetter: LoggerGetter,
    touchHelperFactory: MovableTouchHelperFactory,
    templateParent: InspTemplateView

) : InspView<MediaPath>(
    media, parentInsp,
    view, unitsConverter, animationHelper, loggerGetter, touchHelperFactory,
    templateParent
) {

    private val pathLinePercents: MutableList<Float> = mutableListOf()
    override val logger: KLogger = loggerGetter.getLogger("InspPathView${media.id}")

    override fun setInnerCornerRadius(radius: Float) {
        path?.setPathCornerRadius(getCornerRadiusAbsolute())
        if (media.backgroundColor != 0)
            super.setInnerCornerRadius(radius)
    }

    override fun getMinPossibleDuration(includeDelayBeforeEnd: Boolean): Int {
        val res = super.getMinPossibleDuration(includeDelayBeforeEnd)
        val movementsDuration = media.movements.getMovementsDuration()

        return max(res, movementsDuration + if (includeDelayBeforeEnd) media.delayBeforeEnd else 0)
    }

    override fun onCurrentFrameChanged(newVal: Int, oldVal: Int) {
        super.onCurrentFrameChanged(newVal, oldVal)
        //innerViewPath.redrawPath() //fix for ios, for android it is nothing
    }

    fun setColorFilter(color: Int?) {
        setNewBackgroundColor(color ?: PredefinedColors.WHITE_ARGB)
    }

    fun drawPath(): CommonPath? {
        val actualFrame = currentFrame - getStartFrameShortCut()

//        logger.info { "drawPath actualFrame ${actualFrame}," +
//                " currentFrame ${currentFrame}, duration = ${duration}" }
        return if (actualFrame >= 0) {

            path?.reset()
            path?.movePath(
                unitsConverter,
                actualFrame.toFloat(), media.movements, pathLinePercents,
                media.movementsConnected, media.movementsInterpolator, this
            )

            path
        } else {
            null
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldW: Int, oldH: Int) {
        super.onSizeChanged(w, h, oldW, oldH)
        path?.refreshGradient(media.gradient, w, h)
    }

    fun setNewGradient(gradient: PaletteLinearGradient) {
        media.gradient = gradient
        path?.refreshGradient(gradient, viewWidth, viewHeight)
        path?.refreshPathColor(gradient.getFirstColor())
        innerViewPath?.invalidateColorOrGradient()
    }

    fun setNewColor(color: Int) {
        rememberInitialColors()
        media.color = color
        media.alpha = ArgbColorManager.alphaDegree(color)
        path?.refreshGradient(null, 0, 0)
        path?.refreshPathColor(color)
        innerViewPath?.invalidateColorOrGradient()
    }

    fun getCurrentColor(): Int? {
        return if (media.paintStyle == PaintStyle.STROKE) media.color
        else media.backgroundColor
    }

    override fun onTemplateSizeChanged(width: Int, height: Int) {
        super.onTemplateSizeChanged(width, height)

        if (media.strokeWidth != null) path?.setStrokeWidth(
            unitsConverter.convertUnitToPixelsF(
                media.strokeWidth, templateParent.viewWidth,
                templateParent.viewHeight
            )
        )
        //innerViewPath.redrawPath() //fix for ios, for android it is nothing
    }

    override fun releaseInner() {
        path = null
        innerViewPath = null
        super.releaseInner()
    }

    override fun refresh() {
        super.refresh()
        calcDurations()
        afterCalcDurations(durationIn, durationOut, duration)

        path?.refreshStyle(media.color, media.alpha, media.strokeCap, media.paintStyle)
        innerViewPath?.invalidateColorOrGradient()

        val templateParent = templateParent
        if (media.isMovable == true && templateParent.isInitialized.value) {
            currentFrame = templateParent.currentFrame
            templateParent.changeSelectedView(this)
        }

        templateParent.childHasFinishedInitializing(this)
    }

    override fun rememberInitialColors() {
        if (media.initialColor == null) media.initialColor = media.color
    }

    override fun restoreInitialColors(layer: Int, isBack: Boolean) {
        media.initialColor?.let { setNewColor(it) }
    }
}
