package app.inspiry.stickers.providers

import app.inspiry.stickers.util.StickerProviderUtil
import app.inspiry.stickers.util.StickerProviderUtil.defaultPaletteItems

object BrushStickerCategory: StickerCategory() {

    override fun getSticker(index: Int): PredefinedSticker {
        return when (index) {
            1 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(2), staticFrameForEdit = 12)
            2 -> PredefinedStickerProg(isLoopEnabled = true, paletteItems = defaultPaletteItems(2))
            3 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(2))
            6 -> PredefinedStickerProg(paletteItems = arrayOf("Color_2", "Color_1"))
            8 -> PredefinedStickerProg(staticFrameForEdit = 10)
            14 -> PredefinedStickerProg(isLoopEnabled = true)
            16 -> PredefinedStickerProg(isLoopEnabled = true)
            17 -> PredefinedStickerProg(isSvg = true)
            18 -> PredefinedStickerProg(isSvg = true)
            19 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(3))
            20 -> PredefinedStickerProg(isSvg = true)
            21 -> PredefinedStickerProg(staticFrameForEdit = 21)
            22 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(2))
            23 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(2))

            else -> PredefinedStickerProg()
        }
    }

    override val defaultStickerSize: String
        get() = StickerProviderUtil.PREVIEW_STICKER_SIZE

    override val stickersPrefix: String
        get() = "Brush"

    override val stickersAmount: Int
        get() = 24
}