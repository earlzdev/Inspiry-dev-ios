package app.inspiry.core.opengl.programPresets

import app.inspiry.core.animator.InspAnimator
import app.inspiry.core.media.MediaTexture
import app.inspiry.core.serialization.AnimatorSerializer
import app.inspiry.core.serialization.MediaTextureSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("templateMask")
class TemplateMask (
    val shaderType: ShaderType? = null,
    val invertFragmentAlpha: Boolean? = null,
    val texturesID: MutableList<String> = mutableListOf(),
    val textures: MutableList<@Serializable(with = MediaTextureSerializer::class) MediaTexture> = mutableListOf(),
    val maskBrightness: MaskBrightness? = null,
    val staticOverlay: String? = null,
    val isBlurEffectAvailable: Boolean? = null,
    val isPixelSizeAvailable: Boolean? = null,
    val displacementPixelStep: Float? = null,
    var animatorsIn: MutableList<@Serializable(with = AnimatorSerializer::class) InspAnimator> = mutableListOf(),
    var animatorsOut: MutableList<@Serializable(with = AnimatorSerializer::class) InspAnimator> = mutableListOf(),
    var animatorsAll: MutableList<@Serializable(with = AnimatorSerializer::class) InspAnimator> = mutableListOf(),
    ) {
    @Transient
    var unpacked: Boolean = false
}