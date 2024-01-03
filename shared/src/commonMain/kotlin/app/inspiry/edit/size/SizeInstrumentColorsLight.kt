package app.inspiry.edit.size

import dev.icerock.moko.graphics.Color

class SizeInstrumentColorsLight : SizeInstrumentColors {
    override val isLight: Boolean
        get() = true
    override val background: Color
        get() = Color(0x29, 0x29, 0x29, 0xff)
    override val textAndIcons: Color
        get() = Color(0xFF, 0xFF, 0xFF, 0xff)

}