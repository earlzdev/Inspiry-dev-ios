package app.inspiry.main.model

import app.inspiry.core.data.templateCategory.TemplateCategory
import app.inspiry.core.data.templateCategory.TemplateCategoryProvider
import app.inspiry.core.manager.LicenseManager
import app.inspiry.main.ui.*
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import dev.icerock.moko.resources.AssetResource
import kotlinx.coroutines.flow.MutableStateFlow

class MainScreenViewModel(
    val licenseManager: LicenseManager,
    val categoryProvider: TemplateCategoryProvider
) : ViewModel() {

    val currentPage = MutableStateFlow(MainScreenPages.TEMPLATES)
    val currentCategoryIndex = MutableStateFlow(0)
    val bannerVisible = MutableStateFlow(!licenseManager.hasPremiumState.value)

    fun onRemoveBanner() {
        bannerVisible.value = false
    }

    fun selectPage(newPage: MainScreenPages) {
        currentPage.value = newPage
    }

    fun changeCategory(newIndex: Int) {
        currentCategoryIndex.value = newIndex
    }

    fun getCategories(): List<TemplateCategory> {
        return categoryProvider.getTemplateCategories(licenseManager.hasPremiumState.value)
    }

    fun getResourcesForCategory(category: TemplateCategory): List<AssetResource> {
        return category.templatePaths
    }

    fun getBannerColors() = bannerColors
    fun getTopTabColors() = topTabColors
    fun getBottomCategoriesColors() = bottomCategoriesColors
    fun getBottomCategoriesDimens() = bottomCategoriesDimens
    fun getMainScreenDimens() = mainScreenDimens
    fun getMainScreenColors() = mainScreenColors


    companion object {
        val bannerColors = SubscribeBannerColorsLight()
        val topTabColors = TopTabColorsLight()
        val bottomCategoriesColors = TemplateCategoriesColorsLight()
        val bottomCategoriesDimens = TemplateCategoriesDimensPhone()
        val mainScreenDimens = MainScreenDimensPhone()
        val mainScreenColors = MainScreenColorsLight()
    }
}