package app.inspiry.core.animator.appliers

import app.inspiry.views.InspView
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("backgroundFade")
class BackgroundFadeAnimApplier(override var from: Float = 0f,
                                override var to: Float = 1f) : AnimApplier(), FloatValuesAnimApplier {

    override fun onPreDraw(view: InspView<*>, value: Float) {
        view.setBackgroundAlphaForAnimation(((to - from) * value) + from)
    }
}