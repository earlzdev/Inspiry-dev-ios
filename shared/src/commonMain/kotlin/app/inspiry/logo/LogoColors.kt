package app.inspiry.logo

import app.inspiry.core.util.PredefinedColors
import dev.icerock.moko.graphics.Color

class LogoColors {
     val background: Color
        get() = Color(0x1A, 0x1A, 0x1A, 0xff)
     val topBarText: Color
        get() = PredefinedColors.WHITE
     val tabBgActive: Color
        get() = Color(0x33, 0x33, 0x33, 0xff)
     val tabTextActive: Color
        get() = Color(0xf2, 0xf2, 0xf2, 0xff)
     val tabTextInactive: Color
        get() = Color(0x82, 0x82, 0x82, 0xff)
     val proText: Color
        get() = Color(0xbd, 0xbd, 0xbd, 0xff)
     val proStroke: Color
        get() = tabTextInactive
}