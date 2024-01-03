package app.inspiry.core.media

import app.inspiry.core.opengl.TextureCreator
import app.inspiry.core.opengl.programPresets.ShaderSource
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


class ProgramCreator(
    val vertexShader: String = "",
    val fragmentShader: String = "",
    val textures: List<TextureCreator>,
    @Transient val shaderSource: ShaderSource? = null
) {
    fun getEditableTexturesCount() = textures.filter { it.type.isEdit() }.size
    fun hasShaderGenerator(): Boolean = shaderSource != null
    fun hasShaderPath(): Boolean = vertexShader.isNotEmpty() && fragmentShader.isNotEmpty()
}