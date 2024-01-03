package app.inspiry.palette.model

import app.inspiry.core.serialization.ColorSerializer
import app.inspiry.core.util.ArgbColorManager
import kotlinx.serialization.Serializable

@Serializable
class MediaPaletteChoice(
    @Serializable(with = ColorSerializer::class)
    override var color: Int? = null,
    override var elements: List<String>
) : BasePaletteChoice<String>() {
    override fun toString(): String {
        return "MediaPaletteChoice(color=${color?.let { ArgbColorManager.colorToString(it) }}, elements=$elements)"
    }
}