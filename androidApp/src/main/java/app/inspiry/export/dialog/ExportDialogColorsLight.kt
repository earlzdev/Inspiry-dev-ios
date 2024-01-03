package app.inspiry.export.dialog

import app.inspiry.core.util.PredefinedColors
import app.inspiry.views.infoview.InfoViewColorsLight
import dev.icerock.moko.graphics.Color

class ExportDialogColorsLight : ExportDialogColors {
    override val bg: Color
        get() = Color(0xfa, 0xfa, 0xfa, 0xff)
    override val title: Color
        get() = PredefinedColors.WHITE
    override val itemText: Color
        get() = Color(0x20, 0x20, 0x20, 0xff)
    override val progressIndicator: Color
        get() = InfoViewColorsLight().progressIndicator
    override val isLight: Boolean
        get() = true
}