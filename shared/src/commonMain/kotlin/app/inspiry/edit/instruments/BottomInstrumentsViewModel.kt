package app.inspiry.edit.instruments

import app.inspiry.views.InspView

interface BottomInstrumentsViewModel {

    fun onSelectedViewChanged(newSelected: InspView<*>?) {

    }

    fun onAdditionalPanelClosed() {}

    fun onHide() {
        sendAnalyticsEvent()
    }

    fun sendAnalyticsEvent() {

    }
}