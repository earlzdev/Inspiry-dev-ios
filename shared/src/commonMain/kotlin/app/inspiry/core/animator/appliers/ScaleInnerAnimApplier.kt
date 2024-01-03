package app.inspiry.core.animator.appliers

import app.inspiry.views.InspView
import app.inspiry.views.media.InspMediaView
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("scale_inner")
class ScaleInnerAnimApplier(override var from: Float = 1f, override var to: Float = 1f) : AnimApplier(),
    FloatValuesAnimApplier {

    override fun onPreDraw(view: InspView<*>, value: Float) {

        if (view is InspMediaView) {
            val scale = getValue(value)
            view.setInnerImageScale(scale, scale)
        }
    }
}