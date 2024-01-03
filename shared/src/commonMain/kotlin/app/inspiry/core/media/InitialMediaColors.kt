package app.inspiry.core.media

import app.inspiry.palette.model.PaletteLinearGradient
import kotlinx.serialization.Serializable

@Serializable
data class InitialMediaColors(
    var colorFilter: Int? = null,
    var backgroundGradient: PaletteLinearGradient? = null,
    var backgroundColor: Int? = null,
    var borderColor: Int? = null,
    var alpha: Float? = null
)