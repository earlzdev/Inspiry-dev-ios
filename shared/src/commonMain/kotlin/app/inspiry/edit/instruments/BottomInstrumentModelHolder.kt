package app.inspiry.edit.instruments

import app.inspiry.views.InspView

class BottomInstrumentModelHolder {
    var additionalInstrument: BottomInstrumentsViewModel? = null //color, font, format etc..
        private set

    var previousInstrument: BottomInstrumentsViewModel? =
        null //previous additional instrument, used for animation
        private set

    var tabsInstruments: BottomInstrumentsViewModel? = null //default or text panel
        private set

    var previousTabInstrument: BottomInstrumentsViewModel? = null
        private set


    inline fun <reified Instruments : BottomInstrumentsViewModel> getMainElseAdditional(): Instruments? {
        return tabsInstruments as? Instruments
            ?: additionalInstrument as? Instruments
            ?: previousTabInstrument as? Instruments
            ?: previousInstrument as? Instruments
    }

    private fun setAdditionalModelInner(model: BottomInstrumentsViewModel?) {
        additionalInstrument?.onHide()
        previousInstrument = additionalInstrument
        additionalInstrument = model
    }

    fun setAdditionalModel(model: BottomInstrumentsViewModel) {
        setAdditionalModelInner(model)
    }

    fun removeAdditionalModel() {
        setAdditionalModelInner(null)
    }

    fun setTabsModel(model: BottomInstrumentsViewModel) {
        previousTabInstrument = tabsInstruments
        tabsInstruments = model
    }

    fun onAdditionalPanelClosed() {
        additionalInstrument?.onHide()
        tabsInstruments?.onAdditionalPanelClosed()
    }

    fun removeTabsModel() {
        previousTabInstrument = tabsInstruments
        tabsInstruments = null
    }

    fun updateSelection(newSelection: InspView<*>?) {
        additionalInstrument?.onSelectedViewChanged(newSelection)
        tabsInstruments?.onSelectedViewChanged(newSelection) // default panel ignores this
    }

    fun hasTabs() = tabsInstruments != null
}