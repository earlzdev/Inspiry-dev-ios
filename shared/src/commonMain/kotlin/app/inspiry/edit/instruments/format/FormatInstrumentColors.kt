package app.inspiry.edit.instruments.format

import app.inspiry.core.ui.UIColors
import dev.icerock.moko.graphics.Color

interface FormatInstrumentColors: UIColors {
    val background: Color
    val activeIconColor: Color
    val activeTextColor: Color
    val inactiveIconColor: Color
    val inactiveTextColor: Color
}