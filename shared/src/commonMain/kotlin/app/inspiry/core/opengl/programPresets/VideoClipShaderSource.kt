package app.inspiry.core.opengl.programPresets

class VideoClipShaderSource(
    override val shaderType: ShaderType,
    val textureCount: Int,
    val startTextureIndex: Int,
    val invertFragmentAlpha: Boolean = false,
    override val maskBrightness: MaskBrightness? = null
) : ShaderSource {
    override fun getFragmentShader(fragmentPattern: String): String {
        val res = setShaderNames(fragmentPattern, textureCount, startTextureIndex)

        return setFragmentAlpha(res, invertFragmentAlpha)
    }

    override fun getVertexShader(vertexPattern: String): String {

        return defaultVertexGenerate(vertexPattern, textureCount, startTextureIndex)
    }
}