package app.inspiry.stickers.providers

import app.inspiry.stickers.util.StickerProviderUtil.defaultPaletteItems

object ChristmasStickerCategory: StickerCategory() {
    override fun getSticker(index: Int): PredefinedSticker {
        return when (index) {
            1 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(3), isLoopEnabled = false, isLoopEnabledOnPreview = true)
            3 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(3), isLoopEnabled = false, isLoopEnabledOnPreview = true)
            4 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(2))
            5 -> PredefinedStickerProg(isLoopEnabled = false, isLoopEnabledOnPreview = true)
            8 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(1))
            9 -> PredefinedStickerProg(isLoopEnabled = false, isLoopEnabledOnPreview = true)
            11 -> PredefinedStickerProg(isLoopEnabled = false, isLoopEnabledOnPreview = true)
            14 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(3))
            15 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(1))
            16 -> PredefinedStickerProg(isLoopEnabled = false, isLoopEnabledOnPreview = true)
            18 -> PredefinedStickerProg(isLoopEnabled = false, isLoopEnabledOnPreview = true)
            19 -> PredefinedStickerProg(paletteItems = defaultPaletteItems(3))
            else -> PredefinedStickerProg()
        }
    }

    override val defaultStickerSize: String
        get() = "0.75w"
    override val stickersPrefix: String
        get() = "christmas"
    override val stickersAmount: Int
        get() = 21

}