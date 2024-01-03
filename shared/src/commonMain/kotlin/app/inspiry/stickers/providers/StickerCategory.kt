package app.inspiry.stickers.providers

abstract class StickerCategory {
    abstract fun getSticker(index: Int): PredefinedSticker?
    abstract val stickersPrefix: String
    abstract val stickersAmount: Int
    abstract val defaultStickerSize: String
    val stickersId: String
        get() = stickersPrefix.toLowerCase()
}