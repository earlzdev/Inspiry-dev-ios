package app.inspiry.core.animator.interpolator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.pow

@Serializable
@SerialName("decelerate")
class InspDecelerateInterpolator(var factor: Float) : InspInterpolator() {

    //taken from android.view.animation.DecelerateInterpolator
    override fun getInterpolation(input: Float): Float {
        val result: Float = if (factor == 1.0f) {
            (1.0f - (1.0f - input) * (1.0f - input))
        } else {
            (1.0f - (1.0f - input).pow((2 * factor)))
        }
        return result
    }
}