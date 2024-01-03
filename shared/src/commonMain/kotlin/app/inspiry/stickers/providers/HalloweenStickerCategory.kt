package app.inspiry.stickers.providers

import app.inspiry.stickers.util.StickerProviderUtil.defaultPaletteItems

object HalloweenStickerCategory: StickerCategory() {
    override fun getSticker(index: Int): PredefinedSticker {
        return when (index) {
            1 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(3))
            2 -> PredefinedStickerProg(paletteItems = arrayOf(
                "Color_1_1",
                "Color_2",
                "Color_3"
            ))
            3 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(2))
            4 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(3))
            5 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(2))
            6 -> PredefinedStickerProg(paletteItems = arrayOf(
                "Color_1",
                "Color_1_1",
                "Color_1_2"
            ))
            7 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(2))
            8 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(3))
            9 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(2))
            10 -> PredefinedStickerProg(paletteItems = arrayOf(
                "Color_1",
                "Color_1_1"
            ))
            13 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(2))
            14 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(3))
            15 -> PredefinedStickerProg(paletteItems = arrayOf(
                "Color_1",
                "Color_1_1"
            ))
            16 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(2))
            17 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(3))
            19 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(2))
            20 -> PredefinedStickerProg(paletteItems = arrayOf(
                "Color_1",
                "Color_1_1",
                "Color_1_2"
            ))
            21 -> PredefinedStickerProg(paletteItems = arrayOf(
                "Color_1",
                "Color_1_1",
                "Color_1_2"
            ))
            22 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(3))
            23 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(2))
            24 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(3))


            else -> PredefinedStickerProg()
        }
    }

    override val defaultStickerSize: String
        get() = "0.75w"
    override val stickersPrefix: String
        get() = "halloween"
    override val stickersAmount: Int
        get() = 24

}