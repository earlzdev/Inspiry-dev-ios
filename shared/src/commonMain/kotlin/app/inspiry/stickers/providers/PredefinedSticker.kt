package app.inspiry.stickers.providers

import app.inspiry.core.media.Media
import app.inspiry.core.media.MediaVector
import app.inspiry.core.template.MediaReadWrite
import app.inspiry.core.util.getFileNameWithParent
import app.inspiry.palette.model.MediaPalette
import app.inspiry.stickers.util.StickerProviderUtil

sealed class PredefinedSticker {
    abstract fun createMedia(category: StickerCategory, stickerIndex: Int, mediaReadWrite: MediaReadWrite): MediaWithPath
}

class PredefinedStickerProg(
    val forPremium: Boolean = false, val isSvg: Boolean = false, val isLoopEnabled: Boolean = !isSvg, val isLoopEnabledOnPreview: Boolean = isLoopEnabled,
    val paletteItems: Array<String>? = null, val staticFrameForEdit: Int = MediaVector.STATIC_FRAME_FOR_EDIT_MIDDLE
): PredefinedSticker() {


    override fun createMedia(
        category: StickerCategory,
        stickerIndex: Int,
        mediaReadWrite: MediaReadWrite
    ): MediaWithPath {
        val originalSource =
            "assets://sticker-resources/${category.stickersId}/${category.stickersPrefix}_${stickerIndex}.${if (isSvg) "svg" else "json"}"

        val mediaPalette: MediaPalette = paletteItems?.let {
            StickerProviderUtil.getMediaPaletteForItems(it)
        } ?: MediaPalette()

        return MediaWithPath(
            StickerProviderUtil.getDefaultMediaSticker(
                originalSource = originalSource,
                forPremium = forPremium,
                isLoopEnabled = isLoopEnabledOnPreview,
                mediaPalette = mediaPalette,
                staticFrameForEdit = staticFrameForEdit,
                defaultStickerSize = category.defaultStickerSize
            ), path = originalSource.getFileNameWithParent(), changeLoopStateBeforeSaving = isLoopEnabled != isLoopEnabledOnPreview
        )
    }

}

class PredefinedStickerAsset(private val assetPath: String): PredefinedSticker() {

    override fun createMedia(
        category: StickerCategory,
        stickerIndex: Int,
        mediaReadWrite: MediaReadWrite
    ): MediaWithPath {

        val media: Media = mediaReadWrite.decodeMediaFromAssets(assetPath)
        return MediaWithPath(media, assetPath)
    }
}