package app.inspiry.preview.viewmodel

import app.inspiry.core.data.TemplatePath
import app.inspiry.core.data.templateCategory.TemplateCategoryProvider
import app.inspiry.core.manager.LicenseManager
import app.inspiry.core.media.Template
import app.inspiry.core.util.BoolPref
import app.inspiry.views.template.InspTemplateView
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow

class PreviewViewModel(
    private val licenseManager: LicenseManager,
    private val templateCategoryProvider: TemplateCategoryProvider,
    settings: Settings,
    val templateView: InspTemplateView
) {
    private var isIGLayoutVisible by BoolPref(settings, "show_inst_layout", false)
    var isWaterMarkVisible: MutableStateFlow<Boolean> = MutableStateFlow(false)
    var isIGLayoutVisibleState: MutableStateFlow<Boolean> = MutableStateFlow(isIGLayoutVisible)

    fun updatePreview(template: Template, templatePath: TemplatePath) {
        isWaterMarkVisible.value = isWaterMarkVisible(template, templatePath)
    }

    fun onTemplateLongClick() {
        isIGLayoutVisibleState.value = !isIGLayoutVisibleState.value
        isIGLayoutVisible = isIGLayoutVisibleState.value
    }

    private fun isWaterMarkVisible(template: Template, templatePath: TemplatePath): Boolean {
        return !template.availableForUser(
            licenseManager.hasPremiumState.value,
            templatePath,
            templateCategoryProvider
        )
    }
}