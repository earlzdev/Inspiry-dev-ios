package app.inspiry.stickers.ui

import app.inspiry.core.util.PredefinedColors
import dev.icerock.moko.graphics.Color

class StickersDarkColors : StickersColors {
    override val background: Color
        get() = Color(0x1A, 0x1A, 0x1A, 0xff)
    override val topBarText: Color
        get() = PredefinedColors.WHITE
    override val tabBgActive: Color
        get() = Color(0x33, 0x33, 0x33, 0xff)
    override val tabBgInactive: Color
        get() = Color(0x00, 0x00, 0x00, 0x00)
    override val tabTextActive: Color
        get() = Color(0xf2, 0xf2, 0xf2, 0xff)
    override val tabTextInactive: Color
        get() = Color(0x82, 0x82, 0x82, 0xff)
    override val stickerStrokeActive: Color
        get() = Color(0x33, 0x33, 0x33, 0xff)
    override val stickerStrokeInactive: Color
        get() = Color(0x00, 0x00, 0x00, 0x00)
    override val proText: Color
        get() = Color(0xbd, 0xbd, 0xbd, 0xff)
    override val proStroke: Color
        get() = tabTextInactive
    override val stickerBgActive: Color
        get() = Color(0x27, 0x27, 0x27, 0xff)
    override val stickerBgInactive: Color
        get() = Color(0x1f, 0x1f, 0x1f, 0xff)
}