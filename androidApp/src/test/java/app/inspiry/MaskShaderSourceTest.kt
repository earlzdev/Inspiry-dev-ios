package app.inspiry

import app.inspiry.core.opengl.programPresets.MaskShaderSource
import app.inspiry.core.opengl.programPresets.ShaderType
import org.junit.Test
import kotlin.test.assertEquals

class MaskShaderSourceTest {
    @Test
    fun testShaderMask() {
        val shaderGen2 = MaskShaderSource(ShaderType.COMMON_MASK, 2, 0, null)
        val fragmentPath = shaderGen2.getFragmentShaderPath()
        val vertexPath = shaderGen2.getVertexShaderPath()
        val fragment = shaderGen2.getFragmentShader(testFragment)
        assertEquals(fragmentPath, "assets://shaders-pattern/common_mask/fragment_shader.glsl")
        assertEquals(vertexPath, "assets://shaders-pattern/common_mask/vertex_shader.glsl")
        assertEquals(testResult2, fragment)
        val shaderGen3 = MaskShaderSource(ShaderType.COMMON_MASK, 3, 10, null)
        val fragment3 = shaderGen3.getFragmentShader(testFragment)
        assertEquals(testResult3, fragment3)
        val shaderGen4 = MaskShaderSource(ShaderType.COMMON_MASK_WITH_OVERLAY, 3, 10, null)
        val fragment4 = shaderGen4.getFragmentShader(testFragment)
        assertEquals(fragment4, testResult3)
    }

    @Test
    fun testBrightness() {
        val shaderGen2 = MaskShaderSource(ShaderType.COMMON_MASK, 3, 0, null)
        val brightness = shaderGen2.getBrightnessString("templateMask")
        assertEquals(
            brightness,
            "0.0 + (templateMask.r * 0.299 + templateMask.g * 0.587 + templateMask.b * 0.114) * 1.0 "
        )
    }

    val sourceTwoMaskFragment = """
#extension GL_OES_EGL_image_external : require
precision mediump float;

// Source
uniform samplerExternalOES sTexture_0_;
varying vec2 vTextureCoord_0_;
uniform vec2 uPixelSize_0_;

// Mask
uniform samplerExternalOES sTexture_1_;
varying vec2 vTextureCoord_1_;

vec4 clampToBorder(vec2 textureCoord);

void main() {
    vec4 source = clampToBorder(sTexture_0_);
    vec4 mask = texture2D(sTexture_1_, vTextureCoord_1_);

    gl_FragColor = source + mask;
}

vec4 clampToBorder(vec2 textureCoord) {
    if (textureCoord.x < 0.0 || textureCoord.x > 1.0 || textureCoord.y < 0.0 || textureCoord.y > 1.0) {
        return vec4(1.0, 1.0, 1.0, 1.0);// Default color
    } else {
        return texture2D(sTexture_0_, textureCoord);
    }
}
    """

    val testFragment = "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "\n" +
            "// Source\n" +
            "uniform samplerExternalOES sTexture_0_;\n" +
            "varying vec2 vTextureCoord_0_;\n" +
            "\n" +
            "// Mask\n" +
            "uniform samplerExternalOES sTexture_1_;\n" +
            "varying vec2 vTextureCoord_1_;\n" +
            "\n" +
            "// Overlay\n" +
            "uniform samplerExternalOES sTexture_2_;\n" +
            "varying vec2 vTextureCoord_2_;\n" +
            "\n" +
            "vec4 clampToBorder(vec2 textureCoord);\n" +
            "\n" +
            "void main() {\n" +
            "    vec4 templateMask = texture2D(sTexture_1_, vTextureCoord_1_);\n" +
            "    vec4 overlay = texture2D(sTexture_2_, vTextureCoord_2_);\n" +
            "\n" +
            "    float brightness = 0.0 + (templateMask.r * 0.299 + templateMask.g * 0.587 + templateMask.b * 0.114) * 1.0 ;\n" +
            "    gl_FragColor = mix(overlay, clampToBorder(vTextureCoord_0_), brightness);\n" +
            "}\n" +
            "\n" +
            "vec4 clampToBorder(vec2 textureCoord) {\n" +
            "    if (textureCoord.x < 0.0 || textureCoord.x > 1.0 || textureCoord.y < 0.0 || textureCoord.y > 1.0) {\n" +
            "        return vec4(1.0, 1.0, 1.0, 1.0);// Default color\n" +
            "    } else {\n" +
            "        return texture2D(sTexture_0_, textureCoord);\n" +
            "    }\n" +
            "}"

    val testResult2 = "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "\n" +
            "// Source\n" +
            "uniform samplerExternalOES sTexture0;\n" +
            "varying vec2 vTextureCoord0;\n" +
            "\n" +
            "// Mask\n" +
            "uniform samplerExternalOES sTexture1;\n" +
            "varying vec2 vTextureCoord1;\n" +
            "\n" +
            "// Overlay\n" +
            "\n" +
            "\n" +
            "\n" +
            "vec4 clampToBorder(vec2 textureCoord);\n" +
            "\n" +
            "void main() {\n" +
            "    vec4 templateMask = texture2D(sTexture1, vTextureCoord1);\n" +
            "    vec4 overlay = vec4(0.0, 0.0, 0.0, 0.0);\n" +
            "\n" +
            "    float brightness = 0.0 + (templateMask.r * 0.299 + templateMask.g * 0.587 + templateMask.b * 0.114) * 1.0 ;\n" +
            "    gl_FragColor = mix(overlay, clampToBorder(vTextureCoord0), brightness);\n" +
            "}\n" +
            "\n" +
            "vec4 clampToBorder(vec2 textureCoord) {\n" +
            "    if (textureCoord.x < 0.0 || textureCoord.x > 1.0 || textureCoord.y < 0.0 || textureCoord.y > 1.0) {\n" +
            "        return vec4(1.0, 1.0, 1.0, 1.0);// Default color\n" +
            "    } else {\n" +
            "        return texture2D(sTexture0, textureCoord);\n" +
            "    }\n" +
            "}"
    val testResult3 = "#extension GL_OES_EGL_image_external : require\n" +
            "precision mediump float;\n" +
            "\n" +
            "// Source\n" +
            "uniform samplerExternalOES sTexture10;\n" +
            "varying vec2 vTextureCoord10;\n" +
            "\n" +
            "// Mask\n" +
            "uniform samplerExternalOES sTexture11;\n" +
            "varying vec2 vTextureCoord11;\n" +
            "\n" +
            "// Overlay\n" +
            "uniform samplerExternalOES sTexture12;\n" +
            "varying vec2 vTextureCoord12;\n" +
            "\n" +
            "vec4 clampToBorder(vec2 textureCoord);\n" +
            "\n" +
            "void main() {\n" +
            "    vec4 templateMask = texture2D(sTexture11, vTextureCoord11);\n" +
            "    vec4 overlay = texture2D(sTexture12, vTextureCoord12);\n" +
            "\n" +
            "    float brightness = 0.0 + (templateMask.r * 0.299 + templateMask.g * 0.587 + templateMask.b * 0.114) * 1.0 ;\n" +
            "    gl_FragColor = mix(overlay, clampToBorder(vTextureCoord10), brightness);\n" +
            "}\n" +
            "\n" +
            "vec4 clampToBorder(vec2 textureCoord) {\n" +
            "    if (textureCoord.x < 0.0 || textureCoord.x > 1.0 || textureCoord.y < 0.0 || textureCoord.y > 1.0) {\n" +
            "        return vec4(1.0, 1.0, 1.0, 1.0);// Default color\n" +
            "    } else {\n" +
            "        return texture2D(sTexture10, textureCoord);\n" +
            "    }\n" +
            "}"
}