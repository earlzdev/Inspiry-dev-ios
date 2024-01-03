package app.inspiry.stickers.providers

import app.inspiry.core.media.MediaVector
import app.inspiry.stickers.util.StickerProviderUtil.defaultPaletteItems

object SocialStickerCategory: StickerCategory() {

    override fun getSticker(index: Int): PredefinedSticker {
        return when (index) {
            1 -> PredefinedStickerProg(isLoopEnabled = false, paletteItems = defaultPaletteItems(2),
                staticFrameForEdit = MediaVector.STATIC_FRAME_FOR_EDIT_LAST)
            2 -> PredefinedStickerProg(isLoopEnabled = false, paletteItems = defaultPaletteItems(2),
                staticFrameForEdit = MediaVector.STATIC_FRAME_FOR_EDIT_LAST)
            3 -> PredefinedStickerProg(isLoopEnabled = false, paletteItems = defaultPaletteItems(2),
                staticFrameForEdit = MediaVector.STATIC_FRAME_FOR_EDIT_LAST)
            4 -> PredefinedStickerProg(isLoopEnabled = false, paletteItems = defaultPaletteItems(2),
                staticFrameForEdit = MediaVector.STATIC_FRAME_FOR_EDIT_LAST)
            5 -> PredefinedStickerProg(isLoopEnabled = false, paletteItems = defaultPaletteItems(2),
                staticFrameForEdit = MediaVector.STATIC_FRAME_FOR_EDIT_LAST)
            6 -> PredefinedStickerProg(isLoopEnabled = false, paletteItems = defaultPaletteItems(2),
                staticFrameForEdit = MediaVector.STATIC_FRAME_FOR_EDIT_LAST)
            7 -> PredefinedStickerProg(isLoopEnabled = true, paletteItems = defaultPaletteItems(2))
            8 -> PredefinedStickerProg(isLoopEnabled = false, paletteItems = defaultPaletteItems(2),
                staticFrameForEdit = MediaVector.STATIC_FRAME_FOR_EDIT_LAST)
            9 -> PredefinedStickerProg(isLoopEnabled = false, paletteItems = defaultPaletteItems(2),
                staticFrameForEdit = MediaVector.STATIC_FRAME_FOR_EDIT_LAST)
            10 -> PredefinedStickerProg(isSvg = true)
            11 -> PredefinedStickerProg(isLoopEnabled = false) //todo wrong svg
            12 -> PredefinedStickerProg(isLoopEnabled = false, paletteItems = defaultPaletteItems(2))
            13 -> PredefinedStickerProg(isSvg = true)
            14 -> PredefinedStickerAsset("sticker-resources/social/Social_14_text.json")

            15 -> PredefinedStickerProg(isLoopEnabled = false, paletteItems = defaultPaletteItems(2))
            16 -> PredefinedStickerProg(isLoopEnabled = false, paletteItems = defaultPaletteItems(2),
                staticFrameForEdit = MediaVector.STATIC_FRAME_FOR_EDIT_LAST)
            17 -> PredefinedStickerProg(isLoopEnabled = false, paletteItems = defaultPaletteItems(2),
                staticFrameForEdit = MediaVector.STATIC_FRAME_FOR_EDIT_LAST)
            18 -> PredefinedStickerProg(isLoopEnabled = true, paletteItems = defaultPaletteItems(2))
            19 -> PredefinedStickerProg(isLoopEnabled = false, paletteItems = defaultPaletteItems(2))
            20 -> PredefinedStickerProg(isLoopEnabled = false, paletteItems = defaultPaletteItems(3))
            21 -> PredefinedStickerProg(isLoopEnabled = true, paletteItems = defaultPaletteItems(3))


            else -> PredefinedStickerProg()
        }
    }

    override val defaultStickerSize: String
        get() = "0.5w"

    override val stickersPrefix: String
        get() = "Social"

    override val stickersAmount: Int
        get() = 21
}