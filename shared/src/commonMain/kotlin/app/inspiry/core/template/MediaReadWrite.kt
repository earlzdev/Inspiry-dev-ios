package app.inspiry.core.template

import app.inspiry.core.manager.FileReadWrite
import app.inspiry.core.media.Media
import app.inspiry.core.media.MediaVector
import app.inspiry.core.serialization.MediaSerializer
import app.inspiry.palette.model.PaletteColor
import app.inspiry.textanim.MediaWithRes
import dev.icerock.moko.resources.AssetResource
import kotlinx.serialization.json.Json

class MediaReadWrite(val json: Json, private val fileReadWrite: FileReadWrite) {

    fun openMediaTexts(
        resources: List<AssetResource>
    ): List<MediaWithRes> {

        return resources.map {

            val originalJson = fileReadWrite.readContentFromAssets(it)

            val media = json.decodeFromString(MediaSerializer, originalJson)

            TemplateUtils.postProcessMedia(media)
            media.preprocessPresetMediaText()

            MediaWithRes(media, it)
        }
    }

    fun decodeMediaFromAssets(path: String): Media {
        val jsonString = fileReadWrite.readContentFromAssets(path)
        return json.decodeFromString(MediaSerializer, jsonString)
    }

    fun decodeMediaFromAssets(res: AssetResource): Media {
        val jsonString = fileReadWrite.readContentFromAssets(res)
        return json.decodeFromString(MediaSerializer, jsonString)
    }

    /**
     * this method is called in EditActivity after we have picked a text or a sticker.
     */
    fun openMediaTextAfterSelection(path: String, defaultTextColor: Int): Media {
        val originalJson = fileReadWrite.readContentFromAssets(path)
        return json.decodeFromString(MediaSerializer, originalJson)
            .also {
                if (it.selectTextView() == null) {
                    //should be only sticker
                    processSticker(it, defaultTextColor)
                    it.minDuration = Media.MIN_DURATION_AS_TEMPLATE
                } else {
                    it.preprocessPresetMediaText()
                }
            }
    }

    fun openAndProcessSticker(
        path: String,
        defaultColor: Int
    ): Media {
        return decodeMediaFromAssets(path)
            .also {
                processSticker(it, defaultColor)
            }
    }

    fun processSticker(it: Media, defaultColor: Int) {

        if (it.hasChildText()) {
            it.layoutPosition.width = "wrap_content"
            it.layoutPosition.height = "wrap_content"
        } else {
            it.layoutPosition.width = DEFAULT_STICKER_SIZE
            it.layoutPosition.height = DEFAULT_STICKER_SIZE
            it.keepAspect = true
        }

        //clear animators
        it.animatorsIn = mutableListOf()

        if (REPLACE_STICKERS_COLOR) {
            it.forAllMedias {
                if (it is MediaVector) {
                    if (it.mediaPalette.choices.size <= 1) {
                        it.mediaPalette.mainColor = PaletteColor(defaultColor)
                    }
                }
            }
        }
    }

    companion object {
        const val REPLACE_STICKERS_COLOR = false
        const val DEFAULT_STICKER_SIZE = "0.4w"
    }
}