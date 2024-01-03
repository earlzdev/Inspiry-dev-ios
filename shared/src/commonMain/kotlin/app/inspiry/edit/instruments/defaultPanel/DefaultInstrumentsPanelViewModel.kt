package app.inspiry.edit.instruments.defaultPanel

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.manager.LicenseManager
import app.inspiry.core.ui.CommonMenu
import app.inspiry.edit.instruments.BottomInstrumentsViewModel
import app.inspiry.views.template.InspTemplateView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json

class DefaultInstrumentsPanelViewModel(
    val templateView: InspTemplateView,
    val analyticsManager: AnalyticsManager,
    val licenseManager: LicenseManager,
    val json: Json,
    val menu: CommonMenu<DefaultInstruments>
) : BottomInstrumentsViewModel {

    val activeInstrument = MutableStateFlow<DefaultInstruments?>(null)

    private var onSelect: (DefaultInstruments?) -> Unit = {}

    fun onSelectedAction(action: (DefaultInstruments?) -> Unit) {
        onSelect = action
    }

    override fun onAdditionalPanelClosed() {
        activeInstrument.value = null
    }

    fun selectInstrument(defaultInstrument: DefaultInstruments?) {
        activeInstrument.value = if (defaultInstrument == activeInstrument.value) null else defaultInstrument
        onSelect(activeInstrument.value)
    }

    fun itemHighlight(item: DefaultInstruments): Boolean {
        val selected = activeInstrument.value
        return selected == item || selected == null || !menu.getMenuItem(selected).mayBeSelected
    }

}

enum class DefaultInstruments(val index: Int) {
    DEFAULT_ADD(0),
    DEFAULT_MUSIC(1),
    DEFAULT_COLOR(2),
    DEFAULT_LAYERS(3),
    DEFAULT_FORMAT(4),
    DEFAULT_DEBUG(5)
}