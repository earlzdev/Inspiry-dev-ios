package app.inspiry.slide.ui

import app.inspiry.core.ui.DialogDarkColors
import dev.icerock.moko.graphics.Color

class SlidesPanelDarkColors: DialogDarkColors(), SlidesPanelColors {
    override val trimCurrentTimeLineColor: Color
        get() = Color(0xff, 0xff, 0xff, 0xff)

    override val selectedPageItem: Color
        get() = Color(0xff, 0xff, 0xff, 0xff)

    override val unselectedPageItem: Color
        get() = Color(0x7e, 0x7e, 0x7e, 0xff)

    override val littleElementBg: Color
        get() = Color(0x33, 0x33, 0x33, 0xff)

    override val trimTextCurrentTime: Color
        get() = Color(0xDA, 0xDA, 0xDA, 0xff)

    override val topPanelBackground: Color
        get() = Color(0x29, 0x29, 0x29, 0xff)
}