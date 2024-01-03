package app.inspiry.main.ui

import app.inspiry.core.ui.UIColors
import dev.icerock.moko.graphics.Color

abstract class TopTabColors: UIColors {
    abstract val backgroundColor: Color
    abstract val tabBgActive: Color
    abstract val tabBgInactive: Color
    abstract val textActive: Color
    abstract val textInactive: Color
    abstract val iconActive: Color
    abstract val iconInactive: Color
}