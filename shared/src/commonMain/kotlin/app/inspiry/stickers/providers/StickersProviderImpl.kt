package app.inspiry.stickers.providers

import app.inspiry.core.manager.InspRemoteConfig
import app.inspiry.core.template.MediaReadWrite

class StickersProviderImpl(
    remoteConfig: InspRemoteConfig,
    private val mediaReadWrite: MediaReadWrite
) : StickersProvider {

    private val stickerAvailability = remoteConfig.getLong("sticker_availability").toInt()
    private val categories by lazy { createCategories() }

    private fun createCategories(): Map<String, StickerCategory> {

        val map = mutableMapOf<String, StickerCategory>()

        fun add(category: StickerCategory) {
            map[category.stickersId] = category
        }

        add(SocialStickerCategory)
        add(BrushStickerCategory)
        add(ArrowStickerCategory)
        add(PaperStickerCategory)
        add(BeautyStickerCategory)
        add(ChristmasStickerCategory)
        add(HalloweenStickerCategory)

        return map
    }

    override fun getStickers(category: String): List<MediaWithPath> {

        val categoryData = categories[category]!!
        return getPredefinedStickersForCategory(categoryData, category)
    }

    override fun getCategories(): List<String> = categories.keys.toList()


    private fun getPredefinedStickersForCategory(
        category: StickerCategory,
        categoryName: String
    ): List<MediaWithPath> {

        val result = mutableListOf<MediaWithPath>()

        (0 until category.stickersAmount).forEach {

            val currentItem = it + 1
            val predefinedSticker = category.getSticker(currentItem)

            if (predefinedSticker != null) {
                val mediaWithPath =
                    predefinedSticker.createMedia(category, currentItem, mediaReadWrite)
                val forPremium =
                    getStickersForPremiumData(categoryName, currentItem, stickerAvailability)

                if (forPremium != null)
                    mediaWithPath.media.forPremium = forPremium


                result.add(mediaWithPath)
            }
        }

        return result
    }
}

private val availability1NotPremiumArrowStickers by lazy {
    setOf(1, 2, 4, 10, 14, 16, 18)
}
private val availability1NotPremiumBeautyStickers by lazy {
    setOf(4, 8, 10, 11, 13, 14)
}
private val availability1NotPremiumBrushStickers by lazy {
    setOf(1, 2, 3, 4, 9, 13, 17, 18, 22, 23)
}
private val availability1NotPremiumPaperStickers by lazy {
    setOf(5, 10, 13)
}
private val availability1NotPremiumSocialStickers by lazy {
    setOf(4, 10, 11, 12, 13, 14, 20)
}
private val availability1NotPremiumHalloweenStickers by lazy {
    setOf(1)
}
private val availability1NotPremiumChristmasStickers by lazy {
    (1..21).toSet()
}
private fun getStickersForPremiumData(
    category: String,
    currentIndex: Int,
    stickerAvailability: Int
): Boolean? {
    return if (stickerAvailability != 1 && category != HalloweenStickerCategory.stickersId)
        null
    else {
        val stickersNotForPremium: Set<Int>? = when (category) {
            ArrowStickerCategory.stickersId -> {
                availability1NotPremiumArrowStickers
            }
            BeautyStickerCategory.stickersId -> {
                availability1NotPremiumBeautyStickers
            }
            BrushStickerCategory.stickersId -> {
                availability1NotPremiumBrushStickers
            }
            PaperStickerCategory.stickersId -> {
                availability1NotPremiumPaperStickers
            }
            SocialStickerCategory.stickersId -> {
                availability1NotPremiumSocialStickers
            }
            HalloweenStickerCategory.stickersId -> {
                availability1NotPremiumHalloweenStickers
            }
            ChristmasStickerCategory.stickersId -> {
                availability1NotPremiumChristmasStickers
            }
            else -> null
        }
        if (stickersNotForPremium == null)
            null
        else
            currentIndex !in stickersNotForPremium
    }

}