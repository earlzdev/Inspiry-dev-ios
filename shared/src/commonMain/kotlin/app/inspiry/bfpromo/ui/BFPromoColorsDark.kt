package app.inspiry.bfpromo.ui

import app.inspiry.core.util.PredefinedColors
import dev.icerock.moko.graphics.Color

class BFPromoColorsDark: BFPromoColors {

    override val buttonGradientStart: Color
        get() = Color(0xff, 0xfa, 0x8a, 0xff)

    override val buttonGradientEnd: Color
        get() = Color(0xd2, 0x99, 0x2b, 0xff)

    override val buttonText: Color
        get() = PredefinedColors.BLACK

    override val oldPrice: Color
        get() = Color(0xf2, 0x65, 0x5c, 0xff)

    override val oldPriceDelete: Color
        get() = Color(0xff, 0x56, 0x4b, 0xff)

    override val specialOffer: Color
        get() = PredefinedColors.WHITE

    override val panelBg: Color
        get() = Color(0x18, 0x18, 0x18, 0xff)
    override val newPrice: Color
        get() = Color(0xff, 0xd2, 0x5e, 0xff)

    override val bannerTitleText: Color
        get() = Color(0xf0, 0xd8, 0x00, 0xff)

    override val isLight: Boolean
        get() = false
}