package app.inspiry.views.text

import app.inspiry.core.media.MediaTextDefaults
import app.inspiry.core.util.ArgbColorManager
import app.inspiry.palette.model.AbsPaletteColor
import app.inspiry.palette.model.PaletteColor
import app.inspiry.palette.model.PaletteLinearGradient
import kotlin.math.roundToInt

class SimpleTextBackground(val inspView: InspTextView) : TextBackground() {

    override fun updateDefaults(defaults: MediaTextDefaults) {
        defaultBackground = defaults.backgroundGradient ?: PaletteColor(defaults.backgroundColor)
        defaultAlpha = ArgbColorManager.alphaDegree(defaultBackground?.getFirstColor() ?: 0)
    }

    override fun getBackground(layer: Int): AbsPaletteColor =
        inspView.media.backgroundGradient ?: PaletteColor(inspView.media.backgroundColor)

    override fun useColorBackground(color: Int, layerId: Int) {
        with(inspView) {
            media.backgroundColor = color
            media.backgroundGradient = null

            if (media.lackBackgroundLineColor()) {
                refreshBackgroundColor()
                textView?.onColorChanged() //ios color update only
            } else {
                textView?.onColorChanged()
            }
            invalidateParentIfTexture()
        }
    }

    override fun useGradientBackground(gradient: PaletteLinearGradient) {
        with(inspView) {
            media.backgroundColor = 0
            media.backgroundGradient = gradient

            if (media.lackBackgroundLineColor()) {
                refreshBackgroundColor()
            } else {
                textView?.onColorChanged()
            }

            invalidateParentIfTexture()

        }
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
        val color = getBackground().getFirstColor()
        return ArgbColorManager.alphaDegree(color)
    }

    override fun gradientIsAvailable(): Boolean = true

    override fun colorLayersCount(): Int = 1
    override fun gradientLayersCount(): Int = 1

}