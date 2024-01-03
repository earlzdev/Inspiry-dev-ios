package app.inspiry.edit.ui

import app.inspiry.core.util.PredefinedColors
import dev.icerock.moko.graphics.Color

class EditColorsLight: EditColors {
    override val topBarText: Color
        get() = Color(0x33, 0x33, 0x33, 0xff)
    override val editTextBack: Color
        get() = Color(0xdd, 0xdd, 0xdd, 0xff)
    override val editTextBackWithBlur: Color
        get() = Color(0xcc, 0xcc, 0xcc, 0x88)
    override val saveConfirmationNegativeButton: Color
        get() = Color(0xd8, 0x1b, 0x60, 0xff)
    override val keyboardDoneColor: Color
        get() = Color(0x33, 0x33, 0x33, 0xff)
    override val keyboardDoneBg: Color
        get() = Color(0xc8, 0xc8, 0xc8, 0xff)
    override val instrumentsBar: Color
        get() = Color(0x20, 0x20, 0x20, 0xff)
    override val sharePremiumText: Color
        get() = Color(0x51, 0x61, 0xf6, 0xff)
    override val sharePremiumBg: Color
        get() = Color(0xe3, 0xe6, 0xff, 0xff)

    override val exportBottomPanelBg: Color
        get() = Color(0xe6, 0xea, 0xeb, 0xff)

    override val exportProgressText: Color
        get() = Color(0x57, 0x65, 0xec, 0xff)

    override val exportProgressStart: Color
        get() = Color(0x39, 0xdb, 0xff, 0xff)
    override val exportProgressEnd: Color
        get() = Color(0x6d, 0x28, 0xff, 0xff)

    override val exportImageElseVideoBg: Color
        get() = Color(0xe6, 0xea, 0xeb, 0xff)

    override val exportImageElseVideoSelectedBg: Color
        get() = Color(0x4f, 0x5e, 0xec, 0xff)

    override val exportImageElseVideoSelectedText: Color
        get() = PredefinedColors.WHITE

    override val exportToAppText: Color
        get() = Color(0x51, 0x51, 0x51, 0xff)

    // 5866ED
    override val exportSaveToGalleryText: Color
        get() = Color(0x58, 0x66, 0xed, 0xff)

    override val isLight: Boolean
        get() = true
}