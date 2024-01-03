package app.inspiry.edit.instruments.textPanel

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.media.getAlignIcon
import app.inspiry.core.ui.CommonMenu
import app.inspiry.edit.instruments.BottomInstrumentsViewModel
import app.inspiry.views.InspView
import app.inspiry.views.text.InspTextView
import kotlinx.coroutines.flow.MutableStateFlow

class TextInstrumentsPanelViewModel(
    inspView: InspTextView,
    val analyticsManager: AnalyticsManager,
    val menu: CommonMenu<TextInstruments>
) : BottomInstrumentsViewModel {

    /**
     * public for ios
     */
    val activeTextInstrument = MutableStateFlow<TextInstruments?>(null)
    val currentView = MutableStateFlow(inspView)
    val alignment = MutableStateFlow(getAlignment())
    private var onSelect: (TextInstruments?, InspTextView) -> Unit = {_, _ ->}

    override fun onSelectedViewChanged(newSelected: InspView<*>?) {
        currentView.value = newSelected as? InspTextView ?: return
        alignment.value = getAlignment()
    }

    private fun selectInstrument(textInstrument: TextInstruments?) {
        activeTextInstrument.value = if (textInstrument== activeTextInstrument.value) null else textInstrument
        onSelect(activeTextInstrument.value, currentView.value)
    }

    override fun onAdditionalPanelClosed() {
        activeTextInstrument.value = null
    }

    fun onInstrumentClick(textInstrument: TextInstruments) {
        if (textInstrument == TextInstruments.TEXT_ALIGNMENT) {
            currentView.value.toggleInnerTextGravity()
            menu.setIcon(TextInstruments.TEXT_ALIGNMENT, getAlignment().getAlignIcon())
            activeTextInstrument.value = TextInstruments.TEXT_ALIGNMENT
            alignment.value = getAlignment()
        } else {
            selectInstrument(textInstrument)
        }
    }

    private fun getAlignment() = currentView.value.media.innerGravity

    fun setOnSelect(action: (TextInstruments?, InspTextView) -> Unit) {
        onSelect = action
    }

    fun getCurrentView(): InspTextView? {
        return currentView.value as? InspTextView
    }

    fun itemHighlight(item: TextInstruments): Boolean {
        val selected = activeTextInstrument.value
        return selected == item || selected == null || !menu.getMenuItem(selected).mayBeSelected
    }
}

