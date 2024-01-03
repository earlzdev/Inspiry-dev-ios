package app.inspiry.stickers.providers

import app.inspiry.stickers.util.StickerProviderUtil
import app.inspiry.stickers.util.StickerProviderUtil.defaultPaletteItems

object ArrowStickerCategory: StickerCategory() {
    override fun getSticker(index: Int): PredefinedSticker {
        return when (index) {
            1 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(2))
            7 -> PredefinedStickerProg(
                paletteItems = arrayOf(
                    "7_1 Outlines",
                    "7_2 Outlines",
                    "7_3 Outlines"
                )
            )
            10 -> PredefinedStickerProg(paletteItems = arrayOf("10_2 Color", "10_1 Color"))
            11 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(2))
            12 -> PredefinedStickerProg(
                paletteItems = arrayOf(
                    "12_1 Color",
                    "12_2 Color",
                    "12_3 Color"
                )
            )
            13 -> PredefinedStickerProg(paletteItems = arrayOf("13_2 Color", "13_1 Color"))
            14 -> PredefinedStickerProg(
                paletteItems = arrayOf(
                    "14_2 Outlines",
                    "14_3 Outlines",
                    "14_1 Outlines"
                )
            )
            15 -> PredefinedStickerProg(
                paletteItems = arrayOf(
                    "15_1 Color",
                    "15_2 Color",
                    "15_3 Color"
                )
            )
            16 -> PredefinedStickerProg(
                paletteItems = arrayOf(
                    "16_2 Color",
                    "16_1 Color",
                    "16_3 Color"
                )
            )
            17 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(2))

            else -> PredefinedStickerProg()
        }
    }
    override val defaultStickerSize: String
        get() = StickerProviderUtil.PREVIEW_STICKER_SIZE
    override val stickersPrefix: String
        get() = "Arrow"
    override val stickersAmount: Int
        get() = 18

}