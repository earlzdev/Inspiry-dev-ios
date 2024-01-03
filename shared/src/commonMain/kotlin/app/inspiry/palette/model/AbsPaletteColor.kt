package app.inspiry.palette.model

import app.inspiry.core.serialization.ColorSerializer
import app.inspiry.core.media.GradientOrientation
import app.inspiry.core.util.ArgbColorManager
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt


@Serializable
sealed class AbsPaletteColor() {
    abstract fun getFirstColor(): Int
    abstract fun getWithAlpha(alpha: Int): AbsPaletteColor
    fun getWithAlpha(alpha: Float) = getWithAlpha((alpha * 255).roundToInt())
}

@Serializable
@SerialName("multi-color")
class PaletteMultiColor(val colors: List<@Serializable(with = ColorSerializer::class) Int>) :
    AbsPaletteColor() {

    override fun getFirstColor() = colors[0]

    override fun getWithAlpha(alpha: Int): AbsPaletteColor {
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PaletteMultiColor) return false

        if (colors != other.colors) return false

        return true
    }

    override fun hashCode(): Int {
        return colors.hashCode()
    }

}

@Serializable
@SerialName("single-color")
class PaletteColor(@Serializable(with = ColorSerializer::class) val color: Int) :
    AbsPaletteColor() {

    override fun getFirstColor(): Int {
        return color
    }

    override fun getWithAlpha(alpha: Int): AbsPaletteColor {
        return PaletteColor(ArgbColorManager.colorWithAlpha(color, alpha))
    }



    override fun hashCode(): Int {
        return color
    }

    override fun toString(): String {
        return "PaletteColor(${ArgbColorManager.colorToString(color)})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PaletteColor) return false

        if (color != other.color) return false

        return true
    }


}

@Serializable()
@SerialName("linear")
@Parcelize
class PaletteLinearGradient(
    var orientation: GradientOrientation = GradientOrientation.LEFT_RIGHT,
    val colors: List<@Serializable(with = ColorSerializer::class) Int> = emptyList(),
    val offsets: FloatArray? = null
) : AbsPaletteColor(), Parcelable {

    fun getShaderCoords(left: Float, top: Float, right: Float, bottom: Float): FloatArray {
        val x0: Float
        val x1: Float
        val y0: Float
        val y1: Float
        when (orientation) {
            GradientOrientation.TOP_BOTTOM -> {
                x0 = left
                y0 = top
                x1 = x0
                y1 = bottom
            }
            GradientOrientation.TR_BL -> {
                x0 = right
                y0 = top
                x1 = left
                y1 = bottom
            }
            GradientOrientation.RIGHT_LEFT -> {
                x0 = right
                y0 = top
                x1 = left
                y1 = y0
            }
            GradientOrientation.BR_TL -> {
                x0 = right
                y0 = bottom
                x1 = left
                y1 = top
            }
            GradientOrientation.BOTTOM_TOP -> {
                x0 = left
                y0 = bottom
                x1 = x0
                y1 = top
            }
            GradientOrientation.BL_TR -> {
                x0 = left
                y0 = bottom
                x1 = right
                y1 = top
            }
            GradientOrientation.LEFT_RIGHT -> {
                x0 = left
                y0 = top
                x1 = right
                y1 = y0
            }
            else -> {
                x0 = left
                y0 = top
                x1 = right
                y1 = bottom
            }
        }
        return floatArrayOf(x0, x1, y0, y1)
    }

    override fun getFirstColor(): Int = colors[0]

    override fun getWithAlpha(alpha: Int): AbsPaletteColor {
        val newColors = arrayListOf<Int>()
        for (it in colors) {
            newColors.add(ArgbColorManager.colorWithAlpha(it, alpha))
        }
        return PaletteLinearGradient(orientation, newColors, offsets)
    }


    override fun hashCode(): Int {
        var result = orientation.hashCode()
        result = 31 * result + colors.hashCode()
        return result
    }

    override fun toString(): String {
        return "PaletteLinearGradient(orientation=$orientation, colors=${colors.map { ArgbColorManager.colorToString(it) }}, offsets=${offsets?.contentToString()})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PaletteLinearGradient) return false

        if (orientation != other.orientation) return false
        if (colors != other.colors) return false
        if (offsets != null) {
            if (other.offsets == null) return false
            if (!offsets.contentEquals(other.offsets)) return false
        } else if (other.offsets != null) return false

        return true
    }

}