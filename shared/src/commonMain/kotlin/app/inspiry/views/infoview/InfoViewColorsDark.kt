package app.inspiry.views.infoview

import dev.icerock.moko.graphics.Color

class InfoViewColorsDark: InfoViewColors {

    override val progressIndicator: Color
        get() = Color(0x82, 0xab, 0xf7, 0xff)
    override val text: Color
        get() = Color(0x75, 0x75, 0x75, 0xff)
    override val isLight: Boolean
        get() = false
}