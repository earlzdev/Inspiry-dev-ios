package app.inspiry.core.animator.interpolator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.pow

@Serializable
@SerialName("accelerate")
class InspAccelerateInterpolator(var factor: Float) : InspInterpolator() {

    private var mDoubleFactor: Float = factor * 2

    //taken from android.view.animation.AccelerateInterpolator
    override fun getInterpolation(input: Float): Float {
        return if (factor == 1.0f) {
            input * input
        } else {
            input.pow(mDoubleFactor)
        }
    }

}