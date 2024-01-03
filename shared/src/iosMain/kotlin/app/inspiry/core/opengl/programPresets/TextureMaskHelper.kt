package app.inspiry.core.opengl.programPresets

import app.inspiry.core.media.LayoutPosition
import app.inspiry.core.media.Media
import app.inspiry.core.media.Template
import app.inspiry.views.template.forEachRecursive
import kotlinx.serialization.json.Json

actual object TextureMaskHelper {
    actual val IOS_UNSUPPORTED_SHADER_TYPES =
        arrayOf(ShaderType.DISPLACEMENT, ShaderType.SCALED_SOURCE_MASK, ShaderType.BLUR_REGION_MASK)

    actual fun maskProcessing(
        template: Template,
        json: Json,
        onAppend: ((viewID: String, maskID: String, textureIndex: Int) -> Unit)?
    ) {
        var currentTextureIndex = 0
        template.forEachMedias { medias ->
            val newMedias: MutableMap<Int, MutableList<Media>> = mutableMapOf()
            for ((index, media) in medias.withIndex()) {

                val (templateMask, maskProvider) = TextureMaskProvider.createMaskProvider(
                    media,
                    currentTextureIndex,
                    json
                )
                    ?: continue
                if (!IOS_UNSUPPORTED_SHADER_TYPES.contains(
                        templateMask.shaderType ?: ShaderType.COMMON_MASK
                    ) && templateMask.textures.size > 0
                ) {
                    val texture = templateMask.textures.first() //{ tIndex, texture ->
                    val newMedia = maskProvider.getImageMediaWithTexture(texture)
                    val lp = newMedia.layoutPosition
                    if (lp.width == LayoutPosition.TAKE_FROM_MEDIA) lp.width =
                        LayoutPosition.MATCH_PARENT
                    if (lp.height == LayoutPosition.TAKE_FROM_MEDIA) lp.height =
                        LayoutPosition.MATCH_PARENT
                    newMedia.textureIndex = currentTextureIndex
                    maskProvider.lastTextureIndex++
                    var alreadyUnpacked = false
                    template.medias.forEachRecursive {
                        if (it.id == newMedia.id) alreadyUnpacked = true
                    }
                    if (!alreadyUnpacked) {
                        newMedias[index] = mutableListOf(newMedia)
                    }
                    onAppend?.invoke(media.id!!, newMedia.id!!, 0)
                    //}
                }

                templateMask.texturesID.forEachIndexed { tIndex, tId ->
                    medias.find { it.id == tId }?.textureIndex = -1
                    onAppend?.invoke(media.id!!, tId, tIndex)
                }

                templateMask.unpacked = true
                currentTextureIndex = maskProvider.lastTextureIndex
            }

            newMedias.forEach {
                medias.addAll(it.key, it.value)
            }
        }
    }
}