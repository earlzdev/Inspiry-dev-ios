package app.inspiry.core.animator.interpolator

expect class PathInterpolator {
    fun getInterpolation(input: Float): Float

    constructor(x1: Float, y1: Float, x2: Float, y2: Float)
}