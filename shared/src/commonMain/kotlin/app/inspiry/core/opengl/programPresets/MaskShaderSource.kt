package app.inspiry.core.opengl.programPresets

class MaskShaderSource(
    override val shaderType: ShaderType,
    val textureCount: Int,
    val startTextureIndex: Int,
    override val maskBrightness: MaskBrightness?,
    val staticOverlay: String? = null,
    val invertFragmentAlpha: Boolean = false,
    val displacementPixelStep: Float? = null,
) : ShaderSource {

    private fun checkTextureInRange(textureId: Int) =
        textureId < (textureCount + startTextureIndex)

    override fun getFragmentShader(fragmentPattern: String): String {

        var resultFragmentShader = setShaderNames(fragmentPattern, textureCount, startTextureIndex)
        if (textureCount < 3) resultFragmentShader
            .replace(
                Regex("vec4 overlay =.+;"),
                if (staticOverlay == null) "vec4 overlay = vec4(0.0, 0.0, 0.0, 0.0);"
                else "vec4 overlay = $staticOverlay;"
            ).replace(Regex(".+${textureStringValue(2)}.+"), ""
            ).replace(Regex("DISPLACEMENT_PIXEL_STEP[ ]*=[ ]*\\d+.\\d"), "DISPLACEMENT_PIXEL_STEP = ${displacementPixelStep ?: 40f}"
            ).replace(
                Regex("float brightness =.+;"),
                "float brightness = ${getBrightnessString("templateMask")};")
            .also { resultFragmentShader = it }
        return setFragmentAlpha(resultFragmentShader, invertFragmentAlpha)


    }

    override fun getVertexShader(vertexPattern: String): String {
        return defaultVertexGenerate(vertexPattern, textureCount, startTextureIndex)
    }

}