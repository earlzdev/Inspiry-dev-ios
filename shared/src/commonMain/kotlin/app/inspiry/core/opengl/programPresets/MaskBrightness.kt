package app.inspiry.core.opengl.programPresets

import kotlinx.serialization.Serializable

@Serializable
data class MaskBrightness(
    val redFactor: Float,
    val greenFactor: Float,
    val blueFactor: Float,
    val addValue: Float = 0f,
    val negative: Boolean = false
)
