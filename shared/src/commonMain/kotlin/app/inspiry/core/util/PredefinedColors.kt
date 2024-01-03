package app.inspiry.core.util

import dev.icerock.moko.graphics.Color

object PredefinedColors {
    val WHITE = Color(0xff, 0xff, 0xff, 0xff)
    val BLACK = Color(0x00, 0x00, 0x00, 0xff)
    const val TRANSPARENT = 0

    val WHITE_ARGB by lazy { WHITE.argb.toInt() }
    val BLACK_ARGB by lazy { BLACK.argb.toInt() }
}