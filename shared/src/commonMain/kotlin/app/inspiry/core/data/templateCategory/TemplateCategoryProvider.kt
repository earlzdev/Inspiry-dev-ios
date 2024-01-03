package app.inspiry.core.data.templateCategory

import dev.icerock.moko.resources.AssetResource

interface TemplateCategoryProvider {

    fun getTemplateCategories(isPremium: Boolean): List<TemplateCategory>
    fun getFreeThisWeek(): List<AssetResource>? = null

    companion object {
        const val CATEGORY_ID_TREND = "trend"
        const val CATEGORY_ID_NEW = "new"
        const val CATEGORY_ID_FREE_FOR_WEEK = "free_for_week"
        const val FREE_FOR_WEEK_AMOUNT = 4
    }
}