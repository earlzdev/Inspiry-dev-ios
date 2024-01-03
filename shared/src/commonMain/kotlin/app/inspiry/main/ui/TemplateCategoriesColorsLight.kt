package app.inspiry.main.ui

import dev.icerock.moko.graphics.Color

class TemplateCategoriesColorsLight : TemplateCategoriesColors {
    override val background: Color
        get() = Color(0xFB, 0xFB, 0xFB, 0xFF)
    override val activeBackground: Color
        get() = Color(0xE0, 0xEC, 0xFF, 0xFF)
    override val bottomDivider: Color
        get() = Color(0xF0, 0xF0, 0xF0, 0xFF)
    override val activeText: Color
        get() = Color(0x48, 0x58, 0xF5, 0xFF)
    override val inactiveBackground: Color
        get() = Color(0xFB, 0xFB, 0xFB, 0xFF)
    override val inactiveText: Color
        get() = Color(0x88, 0x88, 0x88, 0xFF)
    override val isLight: Boolean
        get() = true
}