package app.inspiry.video.program

import app.inspiry.core.media.ProgramCreator
import app.inspiry.core.opengl.TextureCreator
import app.inspiry.core.opengl.TransformTextureMatrixData
import app.inspiry.core.util.FileUtils
import app.inspiry.media.TransformTextureMatrix
import app.inspiry.video.SourceUtils

/**
 * Used for all templates with video
 */


object DefaultProgramCreator {

    fun getDefaultProgramCreator(
        source: String,
        isBlurEffectAvailable: Boolean,
        isLoopEnabled: Boolean
    ): ProgramCreator {
        return ProgramCreator(vertexShader = VERTEX_SHADER_SOURCE,
            fragmentShader = getFragmentShaderSource(isBlurEffectAvailable),
            textures = listOf(
                TextureCreator(
                    type = TextureCreator.Type.VIDEO_EDIT,
                    name = 0,
                    source = source,
                    isBlurEffectAvailable = isBlurEffectAvailable,
                    matrices = listOf(TransformTextureMatrixData(0)),
                    isLoopEnabled = isLoopEnabled)
            ))
    }

    private const val PROGRAM_SOURCE_LOCATION =
        "${FileUtils.ASSETS_SCHEME}://template-resources/common_shaders"
    private val VERTEX_SHADER_SOURCE = "$PROGRAM_SOURCE_LOCATION/vertex_shader.glsl"

    private fun getFragmentShaderSource(isBlurEffectAvailable: Boolean): String {
        val fragmentSource = if (isBlurEffectAvailable) "fragment_blur_shader"
        else "fragment_shader"
        return "$PROGRAM_SOURCE_LOCATION/$fragmentSource.glsl"
    }
}