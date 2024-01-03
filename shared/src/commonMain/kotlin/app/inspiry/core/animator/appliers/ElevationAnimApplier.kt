package app.inspiry.core.animator.appliers

import app.inspiry.views.InspView
import app.inspiry.views.media.InspMediaView
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("elevation")
class ElevationAnimApplier(override var from: Float = 0f, override var to: Float) : AnimApplier(),
    FloatValuesAnimApplier {

    override fun onPreDraw(view: InspView<*>, value: Float) {

        val v = view as InspMediaView

        val newElevation = ((view.viewWidth + view.viewHeight) / 10f) * getValue(value)
        v.setNewElevation(newElevation)
    }
}