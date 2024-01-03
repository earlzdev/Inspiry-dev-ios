package app.inspiry.edit.colorChanging

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.edit.instruments.color.*
import app.inspiry.views.InspView
import app.inspiry.views.group.InspGroupView
import app.inspiry.views.media.InspMediaView
import app.inspiry.views.path.InspPathView
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.text.InspTextView
import app.inspiry.views.vector.InspVectorView
import kotlinx.serialization.json.Json

class ColorDialogViewModelProvider(
    val templateView: InspTemplateView? = null,
    val inspView: InspView<*>?,
    val isBack: Boolean = false,
    val analyticsManager: AnalyticsManager,
    val json: Json
) {
    fun create(): ColorDialogViewModel {
        if (templateView == null && inspView == null) throw IllegalArgumentException("Both arguments (InspTemplateView and InspView) is null")
        return when (inspView) {
            is InspTextView -> if (isBack) BackColorChangeViewModel(inspView = inspView, analyticsManager) else TextColorChangeViewModel(inspView = inspView, analyticsManager)
            is InspVectorView -> VectorColorChangeViewModel(inspView = inspView, analyticsManager)
            is InspMediaView -> ImageColorChangeViewModel(inspView = inspView, analyticsManager)
            is InspPathView -> PathColorChangeViewModel(inspView = inspView, analyticsManager)
            is InspGroupView -> GroupColorChangeViewModel(inspView = inspView, analyticsManager)
            null -> TemplatePaletteChangeViewModel(templateView = templateView!!, analyticsManager, json)
            else -> throw IllegalStateException("Unsupported color change dialog for ${inspView.media.id}")
        }
    }
}