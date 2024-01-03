package app.inspiry.video.program

import android.graphics.Bitmap
import android.view.ViewGroup
import app.inspiry.core.opengl.TextureCreator
import app.inspiry.core.opengl.VideoPlayerParams
import app.inspiry.media.TextureMatrix
import app.inspiry.video.gles.Program
import app.inspiry.video.gles.texture.TextureFactory
import app.inspiry.media.TextureParams
import app.inspiry.media.createTextureMatrix
import app.inspiry.core.data.TransformMediaData
import app.inspiry.video.gles.texture.matrix.hasTransformMatrix
import app.inspiry.video.gles.texture.matrix.setTransform
import app.inspiry.video.program.source.DecoderTextureSource
import app.inspiry.video.program.source.ImageTextureSource
import app.inspiry.video.program.source.TemplateTextureSource
import app.inspiry.video.program.source.TextureSource

/**
 * Used for deferred texture creation
 */
class DeferredTextureCreator(val textureCreator: TextureCreator) {

    private var textureSource: TextureSource? = null
    val isEditable = textureCreator.type.isEdit()

    val isInitialized: Boolean
        get() = textureSource != null

    val isTransformTexture: Boolean
        get() = textures.hasTransformMatrix()

    val isImage: Boolean
        get() = textureSource is ImageTextureSource

    val textures: List<TextureMatrix<*>> by lazy { textureCreator.matrices.map { it.createTextureMatrix() } }

    val sourceUri: String?
        get() = (textureSource as? DecoderTextureSource)?.uri
            ?: (textureSource as? ImageTextureSource)?.uri

    val params: TextureParams
        get() = TextureParams(textureCreator.isPixelSizeAvailable, textureCreator.isBlurEffectAvailable, textures)

    fun setImage(bitmap: Bitmap? = null, uri: String? = null) {
        if (textureCreator.type.isEdit()) textureCreator.type = TextureCreator.Type.IMAGE_EDIT
        else require(textureCreator.type.isImage())
        textureSource = ImageTextureSource(bitmap, uri)
    }

    fun setVideo(uri: String, videoTimeOffsetUs: Long, videoVolume: Float,
                 viewStartTimeUs: Long = 0, totalDurationUs: Long = 0) {
        if (textureCreator.type.isEdit()) textureCreator.type = TextureCreator.Type.VIDEO_EDIT
        else require(textureCreator.type.isVideo())

        val playerParams = VideoPlayerParams(viewStartTimeUs, videoTimeOffsetUs,
            totalDurationUs, textureCreator.isLoopEnabled, videoVolume)
        textureSource = DecoderTextureSource(uri, playerParams)
    }

    fun setTemplate(containerView: ViewGroup) {
        require(textureCreator.type == TextureCreator.Type.TEMPLATE)

        textureSource = TemplateTextureSource(containerView)
    }

    /**
     * @return true - if texture has been successfully created
     */
    fun createTexture(program: Program, textureFactory: TextureFactory) =
        textureSource!!.create(program, textureCreator.name, params, textureFactory)

    fun setTransform(textureTransformData: TransformMediaData) {
        textures.setTransform(textureTransformData)
    }
}