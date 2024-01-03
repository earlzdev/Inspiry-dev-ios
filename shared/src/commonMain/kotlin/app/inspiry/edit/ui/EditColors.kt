package app.inspiry.edit.ui

import app.inspiry.core.ui.UIColors
import dev.icerock.moko.graphics.Color

interface EditColors: UIColors {
    val topBarText: Color
    val instrumentsBar: Color
    val sharePremiumText: Color
    val sharePremiumBg: Color

    val exportBottomPanelBg: Color
    val exportProgressText: Color
    val exportProgressStart: Color
    val exportProgressEnd: Color
    val exportImageElseVideoBg: Color
    val exportImageElseVideoSelectedBg: Color
    val exportImageElseVideoSelectedText: Color
    val exportToAppText: Color
    val exportSaveToGalleryText: Color
    val editTextBack: Color
    val editTextBackWithBlur: Color
    val keyboardDoneColor: Color
    val keyboardDoneBg: Color
    val saveConfirmationNegativeButton: Color
}