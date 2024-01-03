package app.inspiry.core.opengl.programPresets

import app.inspiry.core.media.Media
import app.inspiry.core.media.Template
import kotlinx.serialization.json.Json

actual object TextureMaskHelper {
    actual val IOS_UNSUPPORTED_SHADER_TYPES: Array<ShaderType> = arrayOf()
    actual fun maskProcessing(
        template: Template,
        json: Json,
        onAppend: ((viewID: String, maskID: String, textureIndex: Int) -> Unit)?
    ) {
        var nextTextureIndex = 0
        template.forEachMedias { medias ->
            val newMedias: MutableMap<Int, MutableList<Media>> = mutableMapOf()
            for ((index, it) in medias.withIndex()) {

                val (templateMask, maskProvider) = TextureMaskProvider.createMaskProvider(
                    it,
                    nextTextureIndex,
                    json
                )
                    ?: continue

                newMedias[index] = maskProvider.prepareMedias()
                templateMask.texturesID.let { texturesID ->
                    maskProvider.assignTexturesToID(texturesID, medias)
                }
                nextTextureIndex = maskProvider.lastTextureIndex
                templateMask.unpacked = true
            }

            newMedias.forEach {
                medias.addAll(it.key, it.value)
            }
        }

    }
}