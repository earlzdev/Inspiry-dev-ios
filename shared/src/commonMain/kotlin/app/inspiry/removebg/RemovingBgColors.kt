package app.inspiry.removebg

import app.inspiry.core.ui.UIColors
import dev.icerock.moko.graphics.Color

interface RemovingBgColors: UIColors {
    val closeTint: Color
    val closeBg: Color
    val removingBgGradientStart: Color
    val removingBgGradientEnd: Color
    val background: Color
}