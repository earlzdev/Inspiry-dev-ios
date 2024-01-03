package app.inspiry.core.opengl.programPresets

import app.inspiry.core.opengl.programPresets.ShaderType.*

interface ShaderSource {

    val shaderType: ShaderType
    val maskBrightness: MaskBrightness?

    fun defaultMaskBrightness() = MaskBrightness(
        redFactor = 0.299f,
        greenFactor = 0.587f,
        blueFactor = 0.114f
    )

    fun getBrightnessString(maskValueName: String): String {
        (maskBrightness ?: defaultMaskBrightness()).let {
            val sign = if (it.negative) -1f else 1f
            val r = it.redFactor
            val g = it.greenFactor
            val b = it.blueFactor
            val add = it.addValue
            return "$add + ($maskValueName.r * $r + $maskValueName.g * $g + $maskValueName.b * $b) * $sign "
        }
    }

    fun setFragmentAlpha(fragmentShader: String, inverted: Boolean = false): String {
        if (shaderType == COMMON_MASK) return fragmentShader.replace("brightness)", if (inverted) "1.0 - brightness)" else "brightness)")
        return fragmentShader.replace("_alpha_", if (inverted) "1.0 - alpha" else "alpha")
    }

    fun setShaderNames(shader: String, count: Int, startIndex: Int): String {
        var res = shader
        for (i in 0 until count) {
            res = res.replace(textureStringValue(i), (i + startIndex).toString())
        }
        return res
    }

    fun defaultVertexGenerate(vertex: String, count: Int, startIndex: Int): String {
        var headerGroup = ""
        var bodyGroup = ""
        for (id in startIndex until startIndex + count) {
            headerGroup += "varying vec2 vTextureCoord$id;\n"
            headerGroup += "uniform mat4 uTextureMatrix$id;\n"
            bodyGroup += "vTextureCoord$id = (uTextureMatrix$id * aTextureCoord).xy;\n"
        }
        return vertex
            .replace("/*headerGroup*/", headerGroup)
            .replace("/*bodyGroup*/", bodyGroup)
    }

    fun getFragmentShader(fragmentPattern: String): String
    fun getVertexShader(vertexPattern: String): String

    fun getVertexShaderPath() =
        "assets://shaders-pattern/common_mask/vertex_shader.glsl"

    fun getFragmentShaderPath() =
        "assets://shaders-pattern/${shaderType.name.lowercase()}/fragment_shader.glsl"

    fun textureStringValue(number: Int) = "_${number}_"

    companion object {

        fun createShaders(
            shaderType: ShaderType,
            textureCount: Int,
            startTextureIndex: Int,
            invertFragmentAlpha: Boolean = false,
            maskBrightness: MaskBrightness? = null,
            staticOverlay: String? = null,
            displacementPixelStep: Float? = null
        ): ShaderSource {
            when (shaderType) {
                VIDEO_CLIP_MASK, TWO_SOURCE_VIDEO_CLIP -> return VideoClipShaderSource(
                    shaderType = shaderType,
                    textureCount = textureCount,
                    startTextureIndex = startTextureIndex,
                    invertFragmentAlpha = invertFragmentAlpha,
                    maskBrightness = maskBrightness
                )
                else -> return MaskShaderSource(
                    shaderType = shaderType,
                    textureCount = textureCount,
                    startTextureIndex = startTextureIndex,
                    maskBrightness = maskBrightness,
                    staticOverlay = staticOverlay,
                    displacementPixelStep = displacementPixelStep
                )
            }
        }
    }
}

enum class ShaderType {
    COMMON_MASK,
    COMMON_MASK_WITH_OVERLAY,
    VIDEO_CLIP_MASK,
    TWO_SOURCE_MASK,
    TWO_SOURCE_VIDEO_CLIP,
    CHANNEL_SHIFT,
    MASKED_OVERLAY,
    INVERTED_MASK,
    SINGLE_ADDITION,
    DISPLACEMENT,
    BLUR_REGION_MASK,
    SCALED_SOURCE_MASK,
    BLEND_OVERLAY_MASK,
    CLIP_DISPLACE_OVERLAY,
    CLIP_DISPLACE_RSCALE,
    CLIP_DISPLACE_SHIFT,
    CLIP_DISPLACE_PIXEL_STEP,
    CLIP_DISPLACE_SHIFT_RED,
    CLIP_DISPLACE_SHIFT_GREEN,
    DOUBLE_MASK
}