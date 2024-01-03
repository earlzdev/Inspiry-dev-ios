package app.inspiry.core.animator.appliers

import app.inspiry.core.animator.text.DrawBackgroundAnimParam
import app.inspiry.views.InspView
import app.inspiry.views.text.InnerGenericText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("fade")
class FadeAnimApplier(override var from: Float = 0f, override var to: Float = 1f) : AnimApplier(),
    FloatValuesAnimApplier {

    override fun onPreDraw(view: InspView<*>, value: Float) {
        view.setNewAlpha(getValue(value))
    }

    override fun transformText(
        param: DrawBackgroundAnimParam,
        value: Float,
        view: InnerGenericText<*>
    ) {
        param.alpha = ((to - from) * value) + from
    }
}