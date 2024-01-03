package app.inspiry.core.animator.interpolator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.E
import kotlin.math.cos
import kotlin.math.pow

//https://evgenii.com/blog/spring-button-animation-on-android/
@Serializable
@SerialName("spring")
class InspSpringInterpolator(val amplitude: Double, val frequency: Double) : InspInterpolator() {

    override fun getInterpolation(input: Float): Float {
        return (-1 * E.pow(-input / amplitude) * cos(frequency * input) + 1).toFloat()
    }
}