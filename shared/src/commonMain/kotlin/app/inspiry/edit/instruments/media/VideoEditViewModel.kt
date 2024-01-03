package app.inspiry.edit.instruments.media

import app.inspiry.edit.instruments.BottomInstrumentsViewModel
import app.inspiry.views.InspView
import app.inspiry.views.media.InspMediaView
import kotlinx.coroutines.flow.MutableStateFlow

class VideoEditViewModel(inspView: InspMediaView): BottomInstrumentsViewModel {
    val currentView = MutableStateFlow(inspView)

    fun stopPlaying() {
        currentView.value.pauseVideoIfExists()
        currentView.value.templateParent.setFrameForEdit()
    }

    override fun onSelectedViewChanged(newSelected: InspView<*>?) {
        stopPlaying()
        if (newSelected is InspMediaView) { //todo for android need && newSelected.canTrimVideo()
            currentView.value = newSelected
        }
    }
}