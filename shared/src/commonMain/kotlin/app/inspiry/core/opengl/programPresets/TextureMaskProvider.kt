package app.inspiry.core.opengl.programPresets

import app.inspiry.core.media.*
import app.inspiry.core.media.LayoutPosition.Companion.MATCH_PARENT
import app.inspiry.core.media.LayoutPosition.Companion.TAKE_FROM_MEDIA
import app.inspiry.core.opengl.AspectRatioTextureMatrixData
import app.inspiry.core.opengl.ClipTextureMatrixData
import app.inspiry.core.opengl.TextureCreator
import app.inspiry.core.opengl.TextureMatrixData
import app.inspiry.core.util.getExt
import kotlinx.serialization.json.Json


abstract class TextureMaskProvider<MEDIA>(
    val media: MEDIA,
    val startTextureIndex: Int,
    val json: Json
) {
    var lastTextureIndex: Int = startTextureIndex
    val textureList = mutableListOf<TextureCreator>()
    val templateMask: TemplateMask? = (media as TemplateMaskOwner).templateMask
    val medias = mutableListOf<Media>()

    abstract fun prepareMedias(): MutableList<Media>

    fun assignTexturesToID(texturesIDs: MutableList<String>, templateMedias: MutableList<Media>) {
        templateMedias.forEach {
            if (it.id != null) {
                val textureID = texturesIDs.indexOf(it.id)
                if (textureID >= 0) {
                    it.textureIndex = lastTextureIndex
                    addTemplateTexture()
                    lastTextureIndex++
                }
            }
        }
    }

    fun wrapTemplateTexture(
        newMedia: Media,
        baseMedia: Media,
        innerLayoutPosition: LayoutPosition? = null
    ) {
        newMedia.textureIndex = lastTextureIndex
        addTemplateTexture()
        var layoutPosition = newMedia.layoutPosition
        if (layoutPosition.width == TAKE_FROM_MEDIA || layoutPosition.height == TAKE_FROM_MEDIA)
            layoutPosition = baseMedia.layoutPosition
        val group = MediaGroup(
            id = newMedia.id + ".Group",
            textureIndex = lastTextureIndex,
            layoutPosition = layoutPosition,
            isTemporaryMedia = true
        )
        newMedia.layoutPosition =
            innerLayoutPosition ?: LayoutPosition(width = MATCH_PARENT, height = MATCH_PARENT)
        newMedia.isTemporaryMedia = true
        group.medias.add(newMedia)
        medias.add(group)
        lastTextureIndex++
    }

    private fun isFirstTexture() = lastTextureIndex == startTextureIndex
    fun addTemplateTexture() {
        val texture = TextureCreator(
            type = TextureCreator.Type.TEMPLATE,
            name = lastTextureIndex,
            isPixelSizeAvailable = if (isFirstTexture()) templateMask?.isPixelSizeAvailable == true else false,
            isBlurEffectAvailable = if (isFirstTexture()) templateMask?.isBlurEffectAvailable == true else false,
            matrices = listOf(AspectRatioTextureMatrixData(name = lastTextureIndex))
        )
        textureList.add(texture)
    }

    fun addVideoTexture(tm: MediaTexture) {
        val matrices = mutableListOf<TextureMatrixData>()
        var hasClipTextures = false
        tm.clipTextures?.forEachIndexed { index, region ->
            val name = lastTextureIndex + index + 1
            hasClipTextures = true
            matrices.add(ClipTextureMatrixData(name = name, clipRegion = region))
        }

        if (matrices.size == 0) matrices.add(AspectRatioTextureMatrixData(name = lastTextureIndex))

        val texture = TextureCreator(
            type = TextureCreator.Type.VIDEO,
            name = lastTextureIndex,
            source = tm.textureSource,
            matrices = matrices,
            isLoopEnabled = tm.isLoopEnabled,
            isBlurEffectAvailable = tm.isBlurEffectAvailable,
            isPixelSizeAvailable = tm.isPixelSizeAvailable

        )
        textureList.add(texture)
        lastTextureIndex += matrices.size + if (hasClipTextures) 1 else 0
    }

    fun getImageMediaWithTexture(texture: MediaTexture): Media {
        val filePath = texture.textureSource!!
        val fileType = filePath.getExt()
        val layoutPosition = texture.layoutPosition
        val t_media = when (fileType) {
            "json" -> MediaVector(
                originalSource = filePath,
                layoutPosition = layoutPosition,
                scaleType = texture.scaleType
            )
            else -> MediaImage(layoutPosition = layoutPosition).apply {
                originalSource = filePath
                scaleType = texture.scaleType
                isEditable = texture.isEditable
                alpha = texture.alpha
            }
        }
        t_media.apply {
            id = "Texture" + (texture.id ?: lastTextureIndex.toString())
            startFrame = texture.startFrame
            animatorsIn = texture.animatorsIn
            animatorsOut = texture.animatorsOut
            animatorsAll = texture.animatorsAll

        }
        return t_media
    }

    companion object {

        fun createMaskProvider(
            media: Media,
            nextTextureIndex: Int,
            json: Json
        ): Pair<TemplateMask, TextureMaskProvider<*>>? {

            return if (media is MediaImage && media.templateMask != null && !media.templateMask!!.unpacked)
                media.templateMask!! to ImageMaskProvider(media, nextTextureIndex, json)
            else if (media is MediaGroup && media.templateMask != null && !media.templateMask!!.unpacked)
                media.templateMask!! to GroupMaskProvider(media, nextTextureIndex, json)
            else
                null
        }
    }
}

expect object TextureMaskHelper {
    val IOS_UNSUPPORTED_SHADER_TYPES: Array<ShaderType>
    fun maskProcessing(template: Template, json: Json, onAppend: ((viewID: String, maskID: String, textureIndex: Int) -> Unit)? = null)
}
