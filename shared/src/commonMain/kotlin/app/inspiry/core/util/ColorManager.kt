package app.inspiry.core.util

import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

abstract class ColorManager {
    abstract fun red(color: Int): Int
    abstract fun green(color: Int): Int
    abstract fun blue(color: Int): Int
    abstract fun alpha(color: Int): Int
    abstract fun color(red: Int, green: Int, blue: Int, alpha: Int): Int
    abstract fun colorToString(color: Int): String
    abstract fun getFromHSL(hsl: ColorHSL, alpha: Float): Int

    fun applyAlphaToColor(color: Int, alpha: Float): Int {
        val r = red(color)
        val g = green(color)
        val b = blue(color)

        return color(r, g, b, (255f * max(min(alpha, 1f), 0f)).toInt())
    }

    fun color(red: Float, green: Float, blue: Float, alpha: Float): Int {
        return color(
            (red * 255).roundToInt(),
            (green * 255).roundToInt(),
            (blue * 255).roundToInt(),
            (alpha * 255).roundToInt()
        )
    }

    fun alphaDegree(color: Int): Float = alpha(color) / 255f

    fun colorDistance(color1: Int, color2: Int, delta: Float): Int {
        return colorDistance(
            alpha(color1),
            red(color1),
            green(color1),
            blue(color1),
            alpha(color2),
            red(color2),
            green(color2),
            blue(color2),
            delta
        )
    }

    fun colorDistance(
        a1: Int,
        r1: Int,
        g1: Int,
        b1: Int,
        a2: Int,
        r2: Int,
        g2: Int,
        b2: Int,
        delta: Float
    ): Int {
        val reverse = 1f - delta

        val a = (a1 * delta + a2 * reverse).roundToInt()
        val r = (r1 * delta + r2 * reverse).roundToInt()
        val g = (g1 * delta + g2 * reverse).roundToInt()
        val b = (b1 * delta + b2 * reverse).roundToInt()

        return color(r, g, b, a)
    }

    fun colorWithoutAlpha(color: Int) = color(red(color), green(color), blue(color), 255)

    /**
     * @param color: Int color
     * @param alpha: Int alpha 0..255
     */
    fun colorWithAlpha(color: Int, alpha: Int) =
        color(red(color), green(color), blue(color), alpha)

    /**
     * @param color: Int color
     * @param alpha: Float alpha 0..1f
     */
    fun colorWithAlpha(color: Int, alpha: Float) =
        color(red(color), green(color), blue(color), (alpha * 255).roundToInt())

    protected fun Int.toStringComponent(): String =
        this.toString(16).let { if (it.length == 1) "0${it}" else it }

    protected inline fun firstColorComponent(color: Int): Int {
        return color ushr 24
    }

    protected inline fun secondColorComponent(color: Int): Int {
        return color shr 16 and 0xFF
    }

    protected inline fun thirdColorComponent(color: Int): Int {
        return color shr 8 and 0xFF
    }

    protected inline fun forthColorComponent(color: Int): Int {
        return color and 0xFF
    }

    fun getHSL(color: Int): ColorHSL {
        val red = red(color) / 255f
        val green = green(color) / 255f
        val blue = blue(color) / 255f

        val min = min(red, min(green, blue))
        val max = max(red, max(green, blue))

        val hue = when (max) {
            min -> 0f
            red -> (60 * (green - blue) / (max - min) + 360) % 360
            green -> 60 * (blue - red) / (max - min) + 120
            blue -> 60 * (red - green) / (max - min) + 240
            else -> throw IllegalStateException ("bad logic max = $max, min = $min")
        }

        val luminance = (max + min) / 2f

        val saturation = when {
            max == min -> 0f
            luminance <= .5f -> (max - min) / (max + min)
            else -> (max - min) / (2f - max - min)
        }

        return ColorHSL(hue, saturation, luminance)
    }

    fun getRGBfromHSL(hsl: ColorHSL): Triple<Float, Float, Float> {
        val hue = hsl.hue / 360f


        val y = when {
            hsl.lightness < 0.5 -> hsl.lightness * (1 + hsl.saturation)
            else -> hsl.lightness + hsl.saturation - hsl.saturation * hsl.lightness
        }

        val x: Float = 2 * hsl.lightness - y

        var r = max(0f, hueToRGB(x, y, hue + 1.0f / 3.0f))
        var g = max(0f, hueToRGB(x, y, hue))
        var b = max(0f, hueToRGB(x, y, hue - 1.0f / 3.0f))

        r = min(r, 1.0f)
        g = min(g, 1.0f)
        b = min(b, 1.0f)
        return Triple(r, g, b)
    }

    fun hueToRGB(x: Float, y: Float, originalHue: Float): Float {
        var h = originalHue
        if (h < 0) h += 1f
        if (h > 1) h -= 1f
        if (6 * h < 1) {
            return x + (y - x) * 6 * h
        }
        if (2 * h < 1) {
            return y
        }
        return if (3 * h < 2) {
            x + (y - x) * 6 * (2.0f / 3.0f - h)
        } else x
    }

}