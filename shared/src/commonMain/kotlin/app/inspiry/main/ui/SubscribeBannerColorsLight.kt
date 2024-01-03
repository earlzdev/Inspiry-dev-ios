package app.inspiry.main.ui

import dev.icerock.moko.graphics.Color

class SubscribeBannerColorsLight: SubscribeBannerColors() {
    override val gradientColor1: Color
        get() = Color(red = 0x5c, green = 0x59, blue = 0xFC, alpha = 0xe5)
    override val gradientColor2: Color
        get() = Color(red = 0x36, green = 0xcF, blue = 0xFF, alpha = 0xe5)
    override val textColor: Color
        get() = Color(red = 0xFF, green = 0xFF, blue = 0xFF, alpha = 0xFF)
    override val isLight: Boolean
        get() = true

}