package app.inspiry.edit

import app.inspiry.views.InspView
import app.inspiry.views.media.InspMediaView
import app.inspiry.views.text.InspTextView

interface EditCoordinator {
    fun showTextForEditOnTop(textView: InspTextView)
    fun pickNewImage(view: InspView<*>?)
    fun onClickRemoveBackground(view: InspMediaView)
}