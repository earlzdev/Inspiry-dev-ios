package app.inspiry.main.ui

import dev.icerock.moko.graphics.Color

class MainScreenColorsLight : MainScreenColors() {
    override val isLight: Boolean
        get() = true
    override val backgroundColor: Color
        get() = Color(0xFF, 0xFF, 0xFF, 0xFF)
    override val instagramLinkTextColor: Color
        get() = Color(0x51, 0x61, 0xF6, 0xFF)
    override val instagramButtonBack: Color
        get() = Color(0x62, 0x71, 0xF7, 0xFF)
    override val instagramButtonText: Color
        get() = Color(0xFF, 0xFF, 0xFF, 0xFF)
    override val emptyStoriesText: Color
        get() = Color(0x2d, 0x2d, 0x2d, 0xFF)
    override val newStoryButtonBack: Color
        get() = Color(0x62, 0x71, 0xF7, 0xFF)
}