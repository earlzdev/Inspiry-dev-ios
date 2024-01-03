package app.inspiry.core.animator.interpolator

import kotlin.math.absoluteValue

/**
 * The code is taken from
 * Jetpack Compose androidx.compose.animation.core.CubicBezierEasing
 */
class PathInterpolatorCommon {
    val x1: Float
    val y1: Float
    val x2: Float
    val y2: Float

    constructor(x1: Float, y1: Float, x2: Float, y2: Float) {
        this.x1 = x1
        this.y1 = y1
        this.x2 = x2
        this.y2 = y2
    }

    private fun evaluateCubic(a: Float, b: Float, m: Float): Float {
        return 3 * a * (1 - m) * (1 - m) * m +
                3 * b * (1 - m) * /*    */ m * m +
                /*                      */ m * m * m
    }


    fun getInterpolation(input: Float): Float {
        if (input > 0f && input < 1f) {
            var start = 0.0f
            var end = 1.0f
            while (true) {
                val midpoint = (start + end) / 2
                val estimate = evaluateCubic(x1, x2, midpoint)
                if ((input - estimate).absoluteValue < CubicErrorBound)
                    return evaluateCubic(y1, y2, midpoint)
                if (estimate < input)
                    start = midpoint
                else
                    end = midpoint
            }
        } else {
            return input
        }
    }
}

private const val CubicErrorBound: Float = 0.001f