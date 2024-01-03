package app.inspiry.palette.model

import app.inspiry.core.animator.appliers.ColorType
import kotlinx.serialization.Serializable

@Serializable
sealed class BasePalette<CHOICE : BasePaletteChoice<*>> {

    abstract val isAvailable: Boolean
    abstract val bgImageOrGradientCanBeSet: Boolean
    abstract var mainColor: AbsPaletteColor?
    abstract var choices: MutableList<CHOICE>
    abstract var backgroundImage: String?
    abstract var alpha: Float

    abstract fun supportsBgImage(): Boolean
    abstract fun supportsAlpha(): Boolean

    fun getMainColor(): Int? {
        return mainColor?.getFirstColor() ?: (choices.find { it.color != null }?.color)
    }

    abstract fun getLinkedColor(colorType: ColorType): Int?
    abstract fun getColorForElement(elementName: String): Int?
    abstract fun setLinkedColor(colorType: ColorType, color: Int)
    abstract fun setColorForElement(elementName: String, color: Int)
}
