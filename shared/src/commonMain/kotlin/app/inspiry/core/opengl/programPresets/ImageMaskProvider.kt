package app.inspiry.core.opengl.programPresets

import app.inspiry.core.media.*
import app.inspiry.core.media.LayoutPosition.Companion.TAKE_FROM_MEDIA
import app.inspiry.core.opengl.TextureCreator
import app.inspiry.core.opengl.TransformTextureMatrixData
import kotlinx.serialization.json.Json

class ImageMaskProvider(
    media: MediaImage,
    startTextureIndex: Int,
    json: Json
) : TextureMaskProvider<MediaImage>(media, startTextureIndex, json) {

    private fun setImageTexture() {
        val texture = TextureCreator(
            type = TextureCreator.Type.IMAGE_EDIT,
            name = lastTextureIndex,
            isPixelSizeAvailable = true,
            matrices = listOf(
                TransformTextureMatrixData(name = lastTextureIndex)
            )
        )
        textureList.add(texture)
        lastTextureIndex++
    }

    override fun prepareMedias(): MutableList<Media> {
        setImageTexture()
        var shaderType = templateMask?.shaderType
        templateMask?.textures?.forEach {
            if (!it.isVideo) wrapTemplateTexture(
                newMedia = getImageMediaWithTexture(it),
                baseMedia = media,
                innerLayoutPosition = it.innerLayoutPosition
            )
            else {
                addVideoTexture(it)
                if (shaderType == null) shaderType = ShaderType.VIDEO_CLIP_MASK
            }
        }
        if (shaderType == null) shaderType = ShaderType.COMMON_MASK
        val textureCount =
            lastTextureIndex - startTextureIndex + (templateMask?.texturesID?.size ?: 0)
        media.programCreator = ProgramCreator(
            textures = textureList,
            shaderSource = ShaderSource.createShaders(
                shaderType!!,
                textureCount = textureCount,
                startTextureIndex = startTextureIndex,
                invertFragmentAlpha = templateMask?.invertFragmentAlpha == true,
                maskBrightness = templateMask?.maskBrightness,
                staticOverlay = templateMask?.staticOverlay,
                displacementPixelStep = templateMask?.displacementPixelStep
            )

        )
        return medias
    }

    private fun getTextureMedia(originalMedia: MediaImage): Media {
        val mainTexture = originalMedia.copy(json)
        (mainTexture as MediaImage).apply {
            id = "${originalMedia.id ?: "Image"}.Texture"
            layoutPosition = LayoutPosition(width = TAKE_FROM_MEDIA, height = TAKE_FROM_MEDIA)
            templateMask = null
//            relatedMedia = originalMedia
            isTemporaryMedia = true
            isMovable = false
        }

        return mainTexture
    }
}