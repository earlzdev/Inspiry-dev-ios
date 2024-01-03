package app.inspiry.preview.ui

import dev.icerock.moko.graphics.Color

class PreviewColorsLight: PreviewColors {
    override val storiesIconCircle: Color
        get() = Color(0xff, 0xff, 0xff, 0x7f)
    override val progressBackground: Color
        get() = Color(0x97,0x97, 0x97,0x7f)
    override val textWaterMark: Color
        get() = Color(0xa7, 0xa7, 0xa7, 0xff)
}