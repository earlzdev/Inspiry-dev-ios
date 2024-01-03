package app.inspiry.main.ui

import app.inspiry.core.ui.UIColors
import dev.icerock.moko.graphics.Color

abstract class SubscribeBannerColors: UIColors {
    abstract val gradientColor1: Color
    abstract val gradientColor2: Color
    abstract val textColor: Color
}