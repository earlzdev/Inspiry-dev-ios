package app.inspiry.views.text

import app.inspiry.core.media.MediaTextDefaults
import app.inspiry.core.util.ArgbColorManager
import app.inspiry.palette.model.AbsPaletteColor
import app.inspiry.palette.model.PaletteColor
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.views.media.InspMediaView


class ImageAsTextBackground(val inspView: InspMediaView) : TextBackground() {

    override fun updateDefaults(defaults: MediaTextDefaults) {
        defaultBackground = defaults.backgroundGradient ?: PaletteColor(defaults.backgroundColor)
        defaultAlpha = ArgbColorManager.alphaDegree(defaultBackground?.getFirstColor() ?: 0)
    }

    override fun getBackground(layer: Int): AbsPaletteColor {
        return inspView.media.backgroundGradient
            ?: PaletteColor(inspView.media.backgroundColor)
    }

    override fun useColorBackground(color: Int, layerId: Int) {
        inspView.media.backgroundGradient = null
        if (inspView.media.originalSource == null) {
            inspView.media.backgroundColor = color
            inspView.refreshBackgroundColor()
        } else {
            inspView.setColorFilter(color, 1f)
        }
    }

    override fun useGradientBackground(gradient: PaletteLinearGradient) {
        inspView.media.backgroundGradient = gradient
        inspView.setColorFilter(null, null)
    }

    override fun resetBackground(layer: Int) {
        defaultBackground?.let {
            if (it is PaletteLinearGradient) {
                useGradientBackground(it)
                inspView.setColorFilter(null, null)
            } else useColorBackground(it.getFirstColor())
        }
        setAlphaToBackground(defaultAlpha ?: 1f)
    }

    override fun setAlphaToBackground(alpha: Float, layer: Int?) {
        val color = getBackground().getFirstColor()
        useColorBackground(ArgbColorManager.applyAlphaToColor(color, alpha))
    }

    override fun getAlpha(layer: Int?): Float {
        return inspView.media.alpha
    }

    override fun gradientIsAvailable(): Boolean = inspView.media.originalSource == null

    override fun colorLayersCount(): Int = 1
    override fun gradientLayersCount(): Int = 1

}