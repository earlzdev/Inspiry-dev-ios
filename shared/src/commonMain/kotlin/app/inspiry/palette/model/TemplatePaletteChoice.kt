package app.inspiry.palette.model

import app.inspiry.core.serialization.ColorSerializer
import kotlinx.serialization.Required
import kotlinx.serialization.Serializable


@Serializable
class TemplatePaletteChoice(

    //fixme: template palette choice does not support gradients

    @Serializable(with = ColorSerializer::class) override var color: Int? = null,
    @Required override var elements: List<PaletteChoiceElement>
) : BasePaletteChoice<PaletteChoiceElement>() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TemplatePaletteChoice) return false

        if (color != other.color) return false

        return true
    }

    override fun hashCode(): Int {
        return color ?: 0
    }

}

@Serializable
class PaletteChoiceElement(
    @Required val type: String,
    val id: String? = null,
    val colorFilter: PaletteColorFilter? = null
)