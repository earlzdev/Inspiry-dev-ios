package app.inspiry

import app.inspiry.core.data.SizeF
import app.inspiry.core.util.InspMathUtil
import org.junit.Test
import kotlin.test.assertEquals

class TestMath {

    @Test
    fun testAspectRatio() {
        assertEquals(
            InspMathUtil.convertAspectRatio(
                newAspectRatio = 2f,
                size = SizeF(200f, 200f),
                makeBigger = false
            ), SizeF(200f, 100f)
        )

        assertEquals(
            InspMathUtil.convertAspectRatio(
                newAspectRatio = 0.5f,
                size = SizeF(200f, 200f),
                makeBigger = false
            ), SizeF(100f, 200f)
        )


        assertEquals(
            InspMathUtil.convertAspectRatio(
                newAspectRatio = 2f,
                size = SizeF(200f, 200f),
                makeBigger = true
            ), SizeF(400f, 200f)
        )

        assertEquals(
            InspMathUtil.convertAspectRatio(
                newAspectRatio = 0.5f,
                size = SizeF(200f, 200f),
                makeBigger = true
            ), SizeF(200f, 400f)
        )
    }
}