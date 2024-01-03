package app.inspiry.font.ui

import dev.icerock.moko.graphics.Color

class FontDialogColorsDark : FontDialogColors() {
    override val backgroundColor: Color
        get() = Color(0x29, 0x29, 0x29, 0xFF)
    override val styleBg: Color
        get() = Color(0x2B, 0x2B, 0x2B, 0xff)
    override val styleBorderInactive: Color
        get() = Color(0x4F, 0x4F, 0x4F, 0xff)
    override val styleBorderActive: Color
        get() = Color(0xDA, 0xDA, 0xDA, 0xff)
    override val styleTextActive: Color
        get() = Color(0xff, 0xff, 0xff, 0xff)
    override val styleTextInactive: Color
        get() = Color(0x99, 0x99, 0x99, 0xff)
    override val categoryTextActive: Color
        get() = Color(0xE0, 0xE0, 0xE0, 0xff)
    override val categoryTextInactive: Color
        get() = Color(0x82, 0x82, 0x82, 0xff)
    override val categoryBgActive: Color
        get() = Color(0x40, 0x40, 0x40, 0xff)
    override val fontTextActive: Color
        get() = Color(0x4C, 0x8B, 0xFD, 0xff)
    override val fontTextInactive: Color
        get() = Color(0xff, 0xff, 0xff, 0xff)
    override val isLight: Boolean
        get() = false
}