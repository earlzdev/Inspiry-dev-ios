package app.inspiry.edit.instruments.format

import app.inspiry.core.manager.LicenseManager
import app.inspiry.core.media.TemplateFormat
import app.inspiry.edit.instruments.BottomInstrumentsViewModel
import app.inspiry.views.template.InspTemplateView
import kotlinx.coroutines.flow.MutableStateFlow

class FormatSelectorViewModel(
    private val templateView: InspTemplateView,
    val licenseManager: LicenseManager,
    private val formatChangedAction: (TemplateFormat?) -> Unit
) :
    BottomInstrumentsViewModel {

    val currentFormat = MutableStateFlow(templateView.template.format)

    fun onFormatChanged(newFormat: TemplateFormat?) { //to subscribe if null
        if (newFormat != null) {
            currentFormat.value = newFormat
            templateView.changeFormat(newFormat)
        }
        formatChangedAction(newFormat)
    }

    fun getFormats() = FormatsProviderImpl().getFormats()
}