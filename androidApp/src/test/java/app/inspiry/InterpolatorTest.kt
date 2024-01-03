package app.inspiry

import app.inspiry.core.animator.interpolator.InspInterpolator
import app.inspiry.core.animator.interpolator.InspPathInterpolator
import app.inspiry.core.data.SizeF
import app.inspiry.core.util.InspMathUtil
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class InterpolatorTest {

    @Test
    fun testInterpolator() {
        assertEquals(
            InspInterpolator.pathInterpolatorBy("linear25expOut"),
            InspPathInterpolator(0.25f, 0.25f, 0.0f, 1.0f)
        )

        assertEquals(
            InspInterpolator.pathInterpolatorBy("0.35,0,0.1,1"),
            InspPathInterpolator(0.35f, 0f, 0.1f, 1f)
        )

        assertEquals(
            InspInterpolator.pathInterpolatorBy(" 0.1, 0 , 0.1 , 1 "),
            InspPathInterpolator(0.1f, 0f, 0.1f, 1f)
        )

        assertFails {
            InspInterpolator.pathInterpolatorBy(" 0.1, 0 , 0.1 ")
        }

        assertFails("bad interpolator value or unknown Interpolator (a) in 1,2,3,a") {
            InspInterpolator.pathInterpolatorBy("1,2,3,a")
        }
    }
}