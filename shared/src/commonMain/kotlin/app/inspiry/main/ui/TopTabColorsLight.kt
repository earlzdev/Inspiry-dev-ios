package app.inspiry.main.ui

import dev.icerock.moko.graphics.Color

class TopTabColorsLight : TopTabColors() {
    override val isLight: Boolean
        get() = true
    override val backgroundColor: Color
        get() = Color(0xFF, 0xFF, 0xFF, 0xFF)
    override val tabBgActive: Color
        get() = Color(0xFF, 0xFF, 0xFF, 0xFF)
    override val tabBgInactive: Color
        get() = Color(0xFF, 0xFF, 0xFF, 0xFF)
    override val textActive: Color
        get() = Color(0x51, 0x61, 0xF6, 0xFF)
    override val textInactive: Color
        get() = Color(0xA8, 0xA8, 0xA8, 0xFF)
    override val iconActive: Color
        get() = Color(0x51, 0x61, 0xF6, 0xFF)
    override val iconInactive: Color
        get() = Color(0xA8, 0xA8, 0xA8, 0xFF)
}