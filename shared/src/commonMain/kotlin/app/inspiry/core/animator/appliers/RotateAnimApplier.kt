package app.inspiry.core.animator.appliers

import app.inspiry.core.animator.text.DrawBackgroundAnimParam
import app.inspiry.views.InspView
import app.inspiry.views.text.InnerGenericText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("rotate")
class RotateAnimApplier(
    override var from: Float = 0f,
    override var to: Float = 0f,
    var step: Float = 0f) : AnimApplier(), FloatValuesAnimApplier {
    override fun onPreDraw(view: InspView<*>, value: Float) {

        val angle = if (step != 0f) view.duration * step + from else to

        view.animationHelper?.animationRotation = (angle - from) * value + from
        view.updateRotation()
    }

    override fun transformText(
        param: DrawBackgroundAnimParam,
        value: Float,
        view: InnerGenericText<*>
    ) {
        param.rotate = (to - from) * value + from
    }
}