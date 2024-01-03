package app.inspiry.stickers.providers

import app.inspiry.stickers.util.StickerProviderUtil
import app.inspiry.stickers.util.StickerProviderUtil.defaultPaletteItems

object BeautyStickerCategory: StickerCategory() {

    override fun getSticker(index: Int): PredefinedSticker {
        return when (index) {

            1 -> PredefinedStickerProg(isSvg = true)
            2 -> PredefinedStickerProg(paletteItems = arrayOf("2_2 BeautyColor_1", "2_1 BeautyColor_2"))
            3 -> PredefinedStickerProg(paletteItems = arrayOf("3_3 BeautyColor1", "3_4 BeautyColor2"))
            4 -> PredefinedStickerProg(paletteItems = arrayOf("4_4 BeautyColor_1"))
            5 -> PredefinedStickerProg(isSvg = true)
            6 -> PredefinedStickerProg(paletteItems = arrayOf("6_3 BeautyColor_1"))
            7 -> PredefinedStickerProg(paletteItems = arrayOf("7_3 BeautyColor_1"))
            8 -> PredefinedStickerProg(paletteItems = arrayOf("8_3 BeautyColor_1","8_1 BeautyColor_2","8_4 BeautyColor_3"))
            9 -> PredefinedStickerProg(isSvg = true)
            10 -> PredefinedStickerProg(isSvg = true)
            11 -> PredefinedStickerProg(paletteItems = arrayOf("11_2 BeautyColor_1"))
            12 -> PredefinedStickerProg(isSvg = true)
            13 -> PredefinedStickerProg(paletteItems = arrayOf("13_2 BeautyColor_1", "13_1 BeautyColor_2", "13_4 BeautyColor_3"))
            14 -> PredefinedStickerProg(paletteItems = arrayOf("14_3 BeautyColor_1", "14_2 BeautyColor_2"))
            15 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(3))
            16 -> PredefinedStickerProg(isSvg = true)
            17 -> PredefinedStickerProg(paletteItems = arrayOf("17_2 BeautyColor_1"))
            18 -> PredefinedStickerProg(paletteItems = arrayOf("18_3 BeautyColor_1", "18_1 BeautyColor_2", "18_2 BeautyColor_3"))
            else -> PredefinedStickerProg()
        }
    }

    override val defaultStickerSize: String
        get() = StickerProviderUtil.PREVIEW_STICKER_SIZE
    override val stickersPrefix: String
        get() = "Beauty"
    override val stickersAmount: Int
        get() = 18
}