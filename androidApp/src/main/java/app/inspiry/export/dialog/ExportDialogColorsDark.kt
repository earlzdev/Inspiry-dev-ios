package app.inspiry.export.dialog

import app.inspiry.core.util.PredefinedColors
import app.inspiry.views.infoview.InfoViewColorsDark
import dev.icerock.moko.graphics.Color

class ExportDialogColorsDark: ExportDialogColors {
    override val bg: Color
        get() = Color(0x20, 0x20, 0x20, 0xff)
    override val title: Color
        get() = PredefinedColors.WHITE
    override val itemText: Color
        get() = PredefinedColors.WHITE
    override val progressIndicator: Color
        get() = InfoViewColorsDark().progressIndicator
    override val isLight: Boolean
        get() = false
}