package app.inspiry.core.data.templateCategory

import app.inspiry.MR
import app.inspiry.core.data.templateCategory.TemplateCategoryProvider.Companion.CATEGORY_ID_FREE_FOR_WEEK
import app.inspiry.core.data.templateCategory.TemplateCategoryProvider.Companion.CATEGORY_ID_NEW
import app.inspiry.core.data.templateCategory.TemplateCategoryProvider.Companion.CATEGORY_ID_TREND
import app.inspiry.core.data.templateCategory.TemplateCategoryProvider.Companion.FREE_FOR_WEEK_AMOUNT
import app.inspiry.core.manager.InspRemoteConfig
import com.russhwolf.settings.Settings
import dev.icerock.moko.resources.AssetResource

class TemplateCategoryProviderImpl(
    val settings: Settings,
    val remoteConfig: InspRemoteConfig,
    private val getFreeThisWeekIndex: () -> Int?
) :
    TemplateCategoryProvider {

    override fun getTemplateCategories(isPremium: Boolean) = TemplatesList.allTemplates().also {

        val freeThisWeek: List<AssetResource>? =
            getFreeThisWeek()

        //the new category is temporarily disabled
        //remoteConfig.getBoolean("new_category")

        val newCategory = if (false) getNewCategory() else null

        if (newCategory != null) {
            it.add(
                0, TemplateCategory(
                    CATEGORY_ID_NEW, MR.strings.category_new,
                    newCategory
                )
            )
        }

        if (remoteConfig.getBoolean("trend_category")) {

            var trends = getTrendsCategory()
            if (trends.isNotEmpty()) {
                if (freeThisWeek != null) trends =
                    ArrayList(trends).also {
                        it.removeAll(freeThisWeek)
                        if (newCategory != null)
                            it.removeAll(newCategory)
                    }

                it.add(
                    0,
                    TemplateCategory(
                        CATEGORY_ID_TREND,
                        MR.strings.category_trends,
                        trends,
                        icon = TemplateCategoryIcon.FIRE
                    )
                )
            }
        }

        if (freeThisWeek != null) {
            it.add(
                0, TemplateCategory(
                    CATEGORY_ID_FREE_FOR_WEEK,
                    MR.strings.category_free_this_week, freeThisWeek
                )
            )
        }
    }

    override fun getFreeThisWeek(): List<AssetResource>? {

        val freeThisWeekIndex = getFreeThisWeekIndex() ?: return null
        return getFreeForWeekCategory().selectNext(
            freeThisWeekIndex * FREE_FOR_WEEK_AMOUNT,
            FREE_FOR_WEEK_AMOUNT
        )
    }

    companion object {

        fun getNewCategory() = emptyList<AssetResource>()

        fun getFreeForWeekCategory() = listOf(
            MR.assets.templates.paper.Paper3Torn,
            MR.assets.templates.minimal.girl_with_hat,
            MR.assets.templates.art.Art4LeavesOn4Photos,
            MR.assets.templates.film.SlidingBlueGradient,
            MR.assets.templates.art.Art3ThreeNiceGuys,
            MR.assets.templates.paper.TwoWithPaper,
            MR.assets.templates.business.SpringCollectionSale,
            MR.assets.templates.film.ThreeFilmMask,
            MR.assets.templates.gradient.BlurLineTrends,
            MR.assets.templates.film.SingleFilmMask,
            MR.assets.templates.minimal.new_in_stock_3small,
            MR.assets.templates.film.SlidingNeonFrame,
            MR.assets.templates.gradient.SingleAlphaDisplaceMask,
            MR.assets.templates.art.Art1SingleWithBgUnderBrush,
            MR.assets.templates.gradient.ShadowInGradient,
            MR.assets.templates.art.Art2SmoothMask,
        )

        fun getTrendsCategory() = emptyList<AssetResource>()
    }
}

fun <T> List<T>.selectNext(fromIndex: Int, amount: Int): List<T> {
    var currentIndex = fromIndex

    var selected = 0

    val newList = ArrayList<T>(amount)
    while (selected < amount) {

        newList.add(get(currentIndex))

        if (currentIndex < size - 1) {
            currentIndex++
        } else {
            currentIndex = 0
        }

        selected++
    }

    return newList
}
