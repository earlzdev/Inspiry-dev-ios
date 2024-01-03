package app.inspiry

import app.inspiry.core.util.ArgbColorManager
import app.inspiry.core.util.ColorHSL
import okhttp3.internal.toHexString
import org.junit.Test
import kotlin.test.assertEquals

class TestColor {


    @Test
    fun rgbToHSLConvert() {
        var rgb = ArgbColorManager.color(0xBF, 0xBF, 0xBF, 255)
        assertEquals(ArgbColorManager.getHSL(rgb).getRounded(), ColorHSL(0f, 0f, 0.75f))

        rgb = ArgbColorManager.color(0, 0x80, 0x80, 255)
        assertEquals(ArgbColorManager.getHSL(rgb).getRounded(), ColorHSL(180f, 1f, 0.25f))

        rgb = ArgbColorManager.color(0x80, 0x80, 0, 255)
        assertEquals(ArgbColorManager.getHSL(rgb).getRounded(), ColorHSL(60f, 1f, 0.25f))
    }

    @Test
    fun hslToRGBConvert() {
        var hsl = ColorHSL(0f, 0f, 1f)
        var rgb = ArgbColorManager.getFromHSL(hsl, 1f)
        assertEquals(rgb.toHexString(), "ffffffff")

        hsl = ColorHSL(120f, 1f, 0.5f)
        rgb = ArgbColorManager.getFromHSL(hsl, 1f)
        assertEquals(rgb.toHexString(), "ff00ff00")

        hsl = ColorHSL(0f, 1f, 0.25f)
        rgb = ArgbColorManager.getFromHSL(hsl, 1f)
        assertEquals(rgb.toHexString(), "ff800000")
    }
}