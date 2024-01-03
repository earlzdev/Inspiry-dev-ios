package app.inspiry.core.opengl

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class TextureCreator(
    var type: Type,
    val name: Int,
    val matrices: List<TextureMatrixData>,
    val source: String? = null,
    val isPixelSizeAvailable: Boolean = false,
    val isBlurEffectAvailable: Boolean = false,
    val isLoopEnabled: Boolean = false
) {

    @Serializable
    enum class Type {

        @SerialName("image")
        IMAGE,

        @SerialName("video")
        VIDEO,

        @SerialName("template")
        TEMPLATE,

        @SerialName("image_edit")
        IMAGE_EDIT,

        @SerialName("video_edit")
        VIDEO_EDIT;

        fun isEdit() = this == IMAGE_EDIT || this == VIDEO_EDIT
        fun isImage() = this == IMAGE_EDIT || this == IMAGE
        fun isVideo() = this == VIDEO_EDIT || this == VIDEO
    }
}