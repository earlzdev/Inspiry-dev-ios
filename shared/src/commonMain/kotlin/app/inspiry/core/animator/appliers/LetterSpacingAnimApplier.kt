package app.inspiry.core.animator.appliers

import app.inspiry.views.InspView
import app.inspiry.views.text.InspTextView
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("letter_spacing")
class LetterSpacingAnimApplier(override var from: Float, override var to: Float) : AnimApplier(),
    FloatValuesAnimApplier {

    override fun onPreDraw(view: InspView<*>, value: Float) {
        val textView = view as InspTextView

        textView.textView?.letterSpacing =
            ((to - from) * value) + from + textView.media.letterSpacing
    }
}