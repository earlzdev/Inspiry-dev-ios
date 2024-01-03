package app.inspiry.edit.instruments.format

import dev.icerock.moko.graphics.Color

class FormatInstrumentColorsLight : FormatInstrumentColors {
    override val isLight: Boolean
        get() = true
    override val background: Color
        get() = Color(0x29, 0x29, 0x29, 0xff)
    override val activeIconColor: Color
        get() = Color(0xFF, 0xFF, 0xFF, 0xff)
    override val activeTextColor: Color
        get() = Color(0xFF, 0xFF, 0xFF, 0xff)
    override val inactiveIconColor: Color
        get() = Color(0xA8, 0xA8, 0xA8, 0xff)
    override val inactiveTextColor: Color
        get() = Color(0xA8, 0xA8, 0xA8, 0xff)
}