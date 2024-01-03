package app.inspiry.stickers.providers

import app.inspiry.stickers.util.StickerProviderUtil
import app.inspiry.stickers.util.StickerProviderUtil.defaultPaletteItems

object PaperStickerCategory: StickerCategory() {

    override fun getSticker(index: Int): PredefinedSticker {
        return when (index) {

            1 -> PredefinedStickerProg(isLoopEnabled = false, paletteItems = defaultPaletteItems(2))
            2 -> PredefinedStickerProg(isLoopEnabled = false, paletteItems = defaultPaletteItems(3))
            3 -> PredefinedStickerProg(isLoopEnabled = false, paletteItems = defaultPaletteItems(3))
            4 -> PredefinedStickerProg(isSvg = true)
            5 -> PredefinedStickerProg(isSvg = true)
            6 -> PredefinedStickerProg(isLoopEnabled = false, paletteItems = defaultPaletteItems(2))
            7 -> PredefinedStickerProg(isSvg = true)
            8 -> PredefinedStickerProg(isLoopEnabled = true)
            9 -> PredefinedStickerProg(isSvg = true)
            10 -> PredefinedStickerProg(isSvg = true)
            11 -> PredefinedStickerProg(isSvg = true)
            12 -> PredefinedStickerProg(isSvg = true)
            13 -> PredefinedStickerProg(isLoopEnabled = false, paletteItems = defaultPaletteItems(3))
            14 -> PredefinedStickerProg(isLoopEnabled = false, paletteItems = arrayOf("14_2 Color_1", "14_3 Color_2", "14_1 Color_3"))
            15 -> PredefinedStickerProg(isSvg = true)
            16 -> PredefinedStickerProg(isSvg = true)
            17 -> PredefinedStickerProg(isLoopEnabled = false, paletteItems = arrayOf("17_1 Color_1", "17_2 Color_2"))
            18 -> PredefinedStickerProg(isLoopEnabled = false, paletteItems = arrayOf("18_1 Color_1", "18_2 Color_2"))
            19 -> PredefinedStickerProg(isSvg = true)
            20 -> PredefinedStickerProg(isLoopEnabled = false, paletteItems = defaultPaletteItems(2))
            21 -> PredefinedStickerProg(isLoopEnabled = false, paletteItems = defaultPaletteItems(2))
            else -> PredefinedStickerProg()
        }
    }
    override val defaultStickerSize: String
        get() = StickerProviderUtil.PREVIEW_STICKER_SIZE

    override val stickersPrefix: String
        get() = "Paper"
    override val stickersAmount: Int
        get() = 22
}