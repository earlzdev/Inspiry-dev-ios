package app.inspiry.core.animator.interpolator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("overshoot")
class InspOvershootInterpolator(var tension: Float) : InspInterpolator() {

    override fun getInterpolation(input: Float): Float {

        // taken from android.view.animation.OvershootInterpolator
        // _o(t) = t * t * ((tension + 1) * t + tension)
        // o(t) = _o(t - 1) + 1
        var t = input
        t -= 1.0f
        return t * t * ((tension + 1) * t + tension) + 1.0f
    }
}
