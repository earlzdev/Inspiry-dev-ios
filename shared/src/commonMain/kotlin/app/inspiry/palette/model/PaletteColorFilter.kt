package app.inspiry.palette.model

import app.inspiry.core.util.ArgbColorManager
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("colorFilter")
class PaletteColorFilter(
    var hue: Float? = null,
    var saturation: Float? = null,
    var lightness: Float? = null,
    var addSaturation: Float? = null,
    var addLightness: Float? = null,
    var addHue: Float? = null,
    var factorSaturation: Float? = null,
    var factorLightness: Float? = null,
    var factorHue: Float? = null,
    var setOpacity: Float? = null,
) {
    fun applyFilter(color: Int): Int {
        var alpha = ArgbColorManager.alpha(color) / 255f
        val hsl = ArgbColorManager.getHSL(color)

        saturation?.let { hsl.saturation = it }
        lightness?.let { hsl.lightness = it }
        hue?.let { hsl.hue = it }

        factorSaturation?.let { hsl.multiplySaturation(it) }
        factorLightness?.let { hsl.multiplyLightness(it) }
        factorHue?.let { hsl.multiplyHue(it) }

        addSaturation?.let { hsl.addSaturation(it) }
        addLightness?.let { hsl.addLightness(it) }
        addHue?.let { hsl.addHue(it) }
        setOpacity?.let { alpha = it }

        return ArgbColorManager.getFromHSL(hsl, alpha)
    }
}