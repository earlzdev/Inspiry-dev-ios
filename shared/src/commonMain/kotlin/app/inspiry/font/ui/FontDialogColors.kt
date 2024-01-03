package app.inspiry.font.ui

import app.inspiry.core.ui.UIColors
import dev.icerock.moko.graphics.Color

abstract class FontDialogColors: UIColors {
    abstract val backgroundColor: Color
    abstract val styleBg: Color
    abstract val styleBorderInactive: Color
    abstract val styleBorderActive: Color
    abstract val styleTextActive: Color
    abstract val styleTextInactive: Color
    abstract val categoryTextActive: Color
    abstract val categoryTextInactive: Color
    abstract val categoryBgActive: Color
    abstract val fontTextActive: Color
    abstract val fontTextInactive: Color
}