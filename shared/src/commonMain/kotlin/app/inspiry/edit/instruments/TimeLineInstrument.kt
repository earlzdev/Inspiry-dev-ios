package app.inspiry.edit.instruments

import app.inspiry.views.InspView
import app.inspiry.views.template.InspTemplateView

class TimeLineInstrumentModel(val templateView: InspTemplateView, var currentView: InspView<*>?): BottomInstrumentsViewModel {
    private var viewChangedAction: ((view: InspView<*>?) -> Unit)? = null

    fun onViewChanged(action: (view: InspView<*>?) -> Unit) {
        viewChangedAction = action
    }

    override fun onSelectedViewChanged(newSelected: InspView<*>?) {
        currentView = newSelected
        viewChangedAction?.invoke(newSelected)
    }
}