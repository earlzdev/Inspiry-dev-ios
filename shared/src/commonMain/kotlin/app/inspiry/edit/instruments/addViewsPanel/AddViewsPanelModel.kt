package app.inspiry.edit.instruments.addViewsPanel

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.ui.CommonMenu
import app.inspiry.edit.instruments.BottomInstrumentsViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class AddViewsPanelModel(
    val analyticsManager: AnalyticsManager,
    val menu: CommonMenu<AddViewsInstruments>
) : BottomInstrumentsViewModel {

    val activeInstrument = MutableStateFlow<AddViewsInstruments?>(null)

    private var onSelect: (AddViewsInstruments?) -> Unit = {}

    fun onSelectedAction(action: (AddViewsInstruments?) -> Unit) {
        onSelect = action
    }

    override fun onAdditionalPanelClosed() {
        activeInstrument.value = null
    }

    fun selectInstrument(defaultInstrument: AddViewsInstruments?) {
        activeInstrument.value =
            if (defaultInstrument == activeInstrument.value) null else defaultInstrument
        onSelect(activeInstrument.value)
    }

    fun itemHighlight(item: AddViewsInstruments): Boolean {
        val selected = activeInstrument.value
        return selected == item || selected == null || !menu.getMenuItem(selected).mayBeSelected
    }

}

enum class AddViewsInstruments(val index: Int) {
    ADD_TEXT(0),
    ADD_STICKER(1),
    ADD_LOGO(2),
    ADD_FRAME(3)
}
