package app.inspiry.core.animator.appliers

import app.inspiry.views.InspView
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("shapeRotate")
class RotateShapeAnimApplier(var from: Float = 0f, var to: Float = 0f, var step: Float = 0f) : AnimApplier() {
    override fun onPreDraw(view: InspView<*>, value: Float) {

        val angle = if (step != 0f) view.duration * step + from else to

        view.animationHelper?.shapeTransform(rotation =  (angle - from) * value + from)
    }
}