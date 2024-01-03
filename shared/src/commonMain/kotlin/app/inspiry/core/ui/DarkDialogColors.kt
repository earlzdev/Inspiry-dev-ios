package app.inspiry.core.ui

import dev.icerock.moko.graphics.Color

abstract class DialogDarkColors: DialogColors {

    override val isLight: Boolean
        get() = false

    override val background: Color
        get() = Color(0x20, 0x20, 0x20, 0xff)
}