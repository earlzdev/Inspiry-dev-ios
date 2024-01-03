package app.inspiry.stickers.providers

interface StickersProvider {
    fun getCategories(): List<String>
    fun getStickers(category: String): List<MediaWithPath>
}