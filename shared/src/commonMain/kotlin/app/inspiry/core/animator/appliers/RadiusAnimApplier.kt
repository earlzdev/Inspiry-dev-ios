package app.inspiry.core.animator.appliers

import app.inspiry.core.animator.text.DrawBackgroundAnimParam
import app.inspiry.views.InspView
import app.inspiry.views.media.InspMediaView
import app.inspiry.views.text.InnerGenericText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("radius")
class RadiusAnimApplier(override var from: Float = 0f,
                        override var to: Float = 0f) : AnimApplier(), FloatValuesAnimApplier {

    override fun onPreDraw(view: InspView<*>, value: Float) {

        view.setCornerRadius((to - from) * value + from)
        if (view is InspMediaView) view.innerMediaView?.updateBorder()
        view.setCornerRadius(getValue(value))
    }

    override fun transformText(param: DrawBackgroundAnimParam, value: Float, view: InnerGenericText<*>) {
        var start = from
        var end = to
        if (to == from && view.radius != 0f) {
            start = view.radius
            end = view.radius
        }
        param.cornersRadius = (end - start) * value + start
    }
}