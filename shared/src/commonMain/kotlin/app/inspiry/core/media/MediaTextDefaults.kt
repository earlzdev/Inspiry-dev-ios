package app.inspiry.core.media

import app.inspiry.palette.model.MediaPalette
import app.inspiry.palette.model.PaletteLinearGradient
import kotlinx.serialization.Serializable

@Serializable
data class MediaTextDefaults(
    var mediaPalette: MediaPalette? = null,
    var textShadowColor: Int? = null,
    var textStrokeColor: Int? = null,
    var textColor: Int,
    var backgroundColor: Int,
    var shadowColors: MutableList<Int>? = null,
    var backgroundGradient: PaletteLinearGradient? = null,
    var textGradient: PaletteLinearGradient? = null,
)