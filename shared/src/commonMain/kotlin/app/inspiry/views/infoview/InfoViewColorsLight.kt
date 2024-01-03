package app.inspiry.views.infoview

import dev.icerock.moko.graphics.Color

class InfoViewColorsLight: InfoViewColors {

    override val progressIndicator: Color
        get() = Color(0x4c, 0x8b, 0xfd, 0xff)
    override val text: Color
        get() = Color(0xcb, 0xcb, 0xcb, 0xff)
    override val isLight: Boolean
        get() = true
}