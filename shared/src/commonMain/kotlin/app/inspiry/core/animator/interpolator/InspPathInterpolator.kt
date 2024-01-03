package app.inspiry.core.animator.interpolator

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
@SerialName("path")
class InspPathInterpolator(val x1: Float, val y1: Float, val x2: Float, val y2: Float) : InspInterpolator() {

    @Transient
    private val inner: PathInterpolator = PathInterpolator(x1, y1, x2, y2)

    override fun getInterpolation(input: Float) = inner.getInterpolation(input)

    override fun toString(): String {
        return "InspPathInterpolator(x1=$x1, y1=$y1, x2=$x2, y2=$y2)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as InspPathInterpolator

        if (x1 != other.x1) return false
        if (y1 != other.y1) return false
        if (x2 != other.x2) return false
        if (y2 != other.y2) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x1.hashCode()
        result = 31 * result + y1.hashCode()
        result = 31 * result + x2.hashCode()
        result = 31 * result + y2.hashCode()
        result = 31 * result + inner.hashCode()
        return result
    }


}