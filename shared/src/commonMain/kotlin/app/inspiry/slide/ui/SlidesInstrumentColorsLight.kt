package app.inspiry.slide.ui

import dev.icerock.moko.graphics.Color

class SlidesInstrumentColorsLight: SlidesInstrumentColors {
    override val background: Color
        get() = Color(0x29, 0x29, 0x29, 0xff)

    override val selectedSlide: Color
        get() = Color(0x00, 0xC2, 0xFF, 0xff)

    override val newItemColor: Color
        get() = Color(0xFF, 0xFF, 0xFF, 0xff)
}