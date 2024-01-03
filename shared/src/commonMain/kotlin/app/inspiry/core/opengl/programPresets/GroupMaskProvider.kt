package app.inspiry.core.opengl.programPresets

import app.inspiry.core.media.*
import app.inspiry.core.opengl.TextureCreator
import app.inspiry.core.util.getExt
import kotlinx.serialization.json.Json

class GroupMaskProvider(
    media: MediaGroup,
    startTextureIndex: Int,
    json: Json
) :
    TextureMaskProvider<MediaGroup>(media, startTextureIndex, json) {

    override fun prepareMedias(): MutableList<Media> {
        val baseMedia = getBaseMedia()
        lastTextureIndex++
        medias.add(baseMedia)
        var shaderType = templateMask?.shaderType
        templateMask?.textures?.forEach {
            if (!it.isVideo) wrapTemplateTexture(
                getImageMediaWithTexture(it),
                media,
                it.innerLayoutPosition
            )
            else {
                addVideoTexture(it)
                if (shaderType == null) shaderType = ShaderType.VIDEO_CLIP_MASK
            }
        }
        if (shaderType == null) shaderType = ShaderType.COMMON_MASK
        val textureCount =
            lastTextureIndex - startTextureIndex + (templateMask?.texturesID?.size ?: 0)
        baseMedia.programCreator = ProgramCreator(
            textures = textureList,
            shaderSource = ShaderSource.createShaders(
                shaderType = shaderType!!,
                textureCount,
                startTextureIndex,
                invertFragmentAlpha = templateMask?.invertFragmentAlpha == true,
                maskBrightness = templateMask?.maskBrightness,
                staticOverlay = templateMask?.staticOverlay
            )
        )
        return medias
    }

    private fun getBaseMedia(): MediaImage {
        val baseMedia =
            MediaImage(media.layoutPosition, isEditable = false, isTemporaryMedia = true)
        media.textureIndex = lastTextureIndex
        templateMask?.let {
            baseMedia.animatorsAll = it.animatorsAll
            baseMedia.animatorsIn = it.animatorsIn
            baseMedia.animatorsOut = it.animatorsOut
        }
        addTemplateTexture()
        return baseMedia
    }
}