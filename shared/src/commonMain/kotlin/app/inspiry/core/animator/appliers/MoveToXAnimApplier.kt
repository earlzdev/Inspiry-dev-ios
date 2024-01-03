package app.inspiry.core.animator.appliers

import app.inspiry.core.animator.ViewRelativity
import app.inspiry.core.animator.appliers.MoveAnimApplier.Companion.preDrawX
import app.inspiry.core.animator.text.DrawBackgroundAnimParam
import app.inspiry.views.InspView
import app.inspiry.views.text.InnerGenericText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("move_to_x")
class MoveToXAnimApplier(
    override var from: Float = 0f,
    override var to: Float = 0f,
    val relativity: ViewRelativity = ViewRelativity.PARENT,
) : AnimApplier(), FloatValuesAnimApplier {

    override fun onPreDraw(view: InspView<*>, value: Float) {
        preDrawX(from, to, relativity, value, view)
    }

    override fun transformText(
        param: DrawBackgroundAnimParam,
        value: Float,
        view: InnerGenericText<*>
    ) {

        param.translateX = ((to - from) * value + from) * param.width
    }
}