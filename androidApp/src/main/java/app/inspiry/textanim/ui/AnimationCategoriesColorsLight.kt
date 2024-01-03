package app.inspiry.textanim.ui

import dev.icerock.moko.graphics.Color

class AnimationCategoriesColorsLight : AnimationCategoriesColors {
    override val background: Color
        get() = Color(0xFB, 0xFB, 0xFB, 0xFF)
    override val activeBackground: Color
        get() = Color(0x4F, 0x4F, 0x4F, 0xFF)
    override val activeText: Color
        get() = Color(0xFF, 0xFF, 0xFF, 0xFF)
    override val inactiveBackground: Color
        get() = Color( 0x00, 0x00, 0x00, 0x00)
    override val inactiveText: Color
        get() = Color( 0x82, 0x82, 0x82, 0xFF)
    override val isLight: Boolean
        get() = true
}