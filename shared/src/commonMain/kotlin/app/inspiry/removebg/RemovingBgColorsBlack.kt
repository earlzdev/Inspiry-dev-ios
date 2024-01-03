package app.inspiry.removebg

import dev.icerock.moko.graphics.Color

class RemovingBgColorsBlack : RemovingBgColors {

    override val closeTint: Color
        get() = Color(0xee, 0xee, 0xee, 0xff)
    override val closeBg: Color
        get() = Color(0x69, 0x69, 0x69, 0xff)
    override val removingBgGradientStart: Color
        get() = Color(0x99, 0xff, 0xff, 0xff)
    override val removingBgGradientEnd: Color
        get() = Color(0x56, 0x9f, 0xff, 0xff)
    override val isLight: Boolean
        get() = false
    override val background: Color
        get() = Color(0x36, 0x41, 0x54, 0xef)
}