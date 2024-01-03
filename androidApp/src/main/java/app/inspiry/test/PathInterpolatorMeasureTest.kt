package app.inspiry.test

import android.util.Log
import android.view.animation.PathInterpolator
import app.inspiry.core.animator.interpolator.PathInterpolatorCommon
import org.koin.core.time.measureDuration

object PathInterpolatorMeasureTest {
    fun testInterpolatorPerformance() {

        // duration1 3.986
        val duration1 = measureDuration {
            val interpolator = PathInterpolator(0.645f, 0.045f, 0.355f, 1.0f)
            doIterations { interpolator.getInterpolation(it) }
        }

        // duration2 11.385.
        val duration2 = measureDuration {
            val interpolator = PathInterpolatorCommon(0.645f, 0.045f, 0.355f, 1.0f)
            doIterations { interpolator.getInterpolation(it) }
        }

        // Obviously native android method is faster

        Log.i("interpolator-test", "duration1 $duration1, " +
                "duration2 $duration2")
    }

    private inline fun doIterations(action: (Float) -> Unit) {
        val max = 100000
        for (i in 1..max) {
            val fraction = i / max.toFloat()
            action(fraction)
        }
    }
}