package app.inspiry.views.text

import app.inspiry.core.media.MediaTextDefaults
import app.inspiry.core.util.ArgbColorManager
import app.inspiry.palette.model.AbsPaletteColor
import app.inspiry.palette.model.PaletteColor
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.views.group.InspGroupView
import kotlin.math.roundToInt


class GroupAsTextBackground(val inspView: InspGroupView) : TextBackground() {


    override fun getBackground(layer: Int): AbsPaletteColor {
        return inspView.media.backgroundGradient
            ?: PaletteColor(inspView.media.backgroundColor)
    }

    private fun refreshGroupBackground() {
        with(inspView) {
            updateBackgroundForAnimation()
            animationHelper?.resetLastTimeTriggeredEnd()
            animationHelper?.preDrawAnimations(currentFrame)
        }
    }

    override fun useColorBackground(color: Int, layerId: Int) {
        inspView.media.backgroundGradient = null
        inspView.setNewBackgroundColor(color)
        refreshGroupBackground()
    }

    override fun useGradientBackground(gradient: PaletteLinearGradient) {
        if (!wasChanged) defaultBackground = gradient
        inspView.media.backgroundColor = 0
        inspView.media.backgroundGradient = gradient
        refreshGroupBackground()
    }

    override fun resetBackground(layer: Int) {
        defaultBackground?.let {
            if (it is PaletteLinearGradient) useGradientBackground(it)
            else useColorBackground(it.getFirstColor())
        }
    }

    override fun setAlphaToBackground(alpha: Float, layer: Int?) {
        val background = getBackground()

        if (background is PaletteLinearGradient)
            useGradientBackground(background.getWithAlpha((alpha * 255).roundToInt()) as PaletteLinearGradient)
        else {
            val color = getBackground().getFirstColor()
            useColorBackground(ArgbColorManager.applyAlphaToColor(color, alpha))
        }
    }

    override fun getAlpha(layer: Int?): Float {
        return ArgbColorManager.alphaDegree(getBackground().getFirstColor())
    }

    override fun gradientIsAvailable(): Boolean = true
    override fun colorLayersCount(): Int = 1
    override fun gradientLayersCount(): Int = 1
    override fun updateDefaults(defaults: MediaTextDefaults) {
            defaultBackground = defaults.backgroundGradient ?: PaletteColor(defaults.backgroundColor)
            defaultAlpha = ArgbColorManager.alphaDegree(defaultBackground?.getFirstColor() ?: 0)
    }

}