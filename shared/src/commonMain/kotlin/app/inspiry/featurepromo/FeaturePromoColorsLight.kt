package app.inspiry.featurepromo

import app.inspiry.core.util.PredefinedColors
import dev.icerock.moko.graphics.Color

class FeaturePromoColorsLight : FeaturePromoColors {
    override val buttonGradientStart: Color
        get() = Color(0xf0, 0x2f, 0xc2, 0xff)
    override val buttonGradientEnd: Color
        get() = Color(0x03, 0x62, 0xff, 0xff)
    override val background: Color
        get() = PredefinedColors.WHITE
    override val closeTint: Color
        get() = Color(0xa8, 0xa8, 0xa8, 0xff)
    override val templateTextSubtitle: Color
        get() = Color(0x24, 0x21, 0x9c, 0xff)
    override val isLight: Boolean = true
}