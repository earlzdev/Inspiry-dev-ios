package app.inspiry.core.opengl.programPresets


import app.inspiry.core.media.Media
import app.inspiry.core.serialization.MediaSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("maskPreset")
class MaskPreset {
    var vertexShaderTemplate: String = ""
    var fragmentShaderTemplate: String = ""
    val textures: MutableList<@Serializable(with = MediaSerializer::class) Media> = mutableListOf()

}