package app.inspiry.views.text

import app.inspiry.core.media.MediaTextDefaults
import app.inspiry.core.util.ArgbColorManager
import app.inspiry.palette.model.AbsPaletteColor
import app.inspiry.palette.model.PaletteColor
import app.inspiry.palette.model.PaletteLinearGradient
import app.inspiry.views.vector.InspVectorView

class VectorAsTextBackground(val inspView: InspVectorView) : TextBackground() {

    override fun updateDefaults(defaults: MediaTextDefaults) {
        defaultBackground = getBackground()
        defaultAlpha = getAlpha()
    }

    override fun getBackground(layer: Int): AbsPaletteColor? {
        val color = inspView.getColorForLayer(layer) ?: return null
        return PaletteColor(ArgbColorManager.applyAlphaToColor(color, getAlpha()))
    }

    override fun useColorBackground(color: Int, layerId: Int) {
        if (colorLayersCount() == 1) inspView.setColorFilter(color)
        else inspView.setColorForLayer(layerId, color)
    }

    override fun useGradientBackground(gradient: PaletteLinearGradient) {
        //vector don't use gradient
    }

    override fun resetBackground(layer: Int) {
        inspView.setColorForLayer(layer, null)
        setAlphaToBackground(defaultAlpha ?: 1f)
    }

    override fun setAlphaToBackground(alpha: Float, layer: Int?) {
        inspView.media.mediaPalette.alpha = alpha
        inspView.updateAlpha()
    }

    override fun getAlpha(layer: Int?): Float {
        return inspView.media.mediaPalette.alpha
    }

    override fun gradientIsAvailable(): Boolean = false

    override fun colorLayersCount(): Int = inspView.colorLayerCount()
    override fun gradientLayersCount(): Int = 0
}