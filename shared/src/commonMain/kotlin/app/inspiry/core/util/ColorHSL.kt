package app.inspiry.core.util

import kotlin.math.abs
import kotlin.math.roundToInt

class ColorHSL(
    var hue: Float,
    var saturation: Float,
    var lightness: Float,
) {
    init {
        if (!(0f..360f).contains(hue) || !(0f..1f).contains(saturation) || !(0f..1f).contains(lightness)) throw IllegalArgumentException("bad color HSL values (h(0..360), s(0..1) l(0..1): ${this.toString()}")
    }

    private fun correctValues() {
        if (saturation < 0f) saturation = 0f
        if (saturation > 1f) saturation = 1f

        if (lightness < 0f) lightness = 0f
        if (lightness > 1f) lightness = 1f

        if (abs(hue) > 360) hue %= 360
        if (hue < 0) hue = 360 - hue
    }

    fun addSaturation(value: Float) {
        saturation += value
        correctValues()
    }

    fun addLightness(value: Float) {
        lightness += value
        correctValues()
    }
    fun addHue( value: Float) {
        hue += value
        correctValues()
    }

    fun multiplySaturation(value: Float) {
        saturation *= value
        correctValues()
    }

    fun multiplyLightness(value: Float) {
        lightness *= value
        correctValues()
    }

    fun multiplyHue(value: Float) {
        hue *= value
        correctValues()
    }
    fun getRounded() = this.apply {
        hue = hue.roundToInt() + 0f
        saturation = (saturation * 100).roundToInt() / 100f
        lightness = (lightness * 100).roundToInt() / 100f
    }

    override fun toString(): String {
        return "ColorHSL(hue=$hue, saturation=$saturation, lightness=$lightness)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ColorHSL

        if (hue != other.hue) return false
        if (saturation != other.saturation) return false
        if (lightness != other.lightness) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hue.hashCode()
        result = 31 * result + saturation.hashCode()
        result = 31 * result + lightness.hashCode()
        return result
    }

}