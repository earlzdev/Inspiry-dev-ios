package app.inspiry.core.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.json.Json

fun String?.parseColor(fallbackColor: Int = PredefinedColors.TRANSPARENT): Int {
    return if (this.isNullOrEmpty()) fallbackColor
    else try {

        // Use a long to avoid rollovers on #ffXXXXXX
        var color: Long = substring(1).toLong(16)
        if (length == 7) {
            // Set the alpha value
            color = color or -0x1000000
        } else require(length == 9) { "Unknown color" }
        return color.toInt()

    } catch (e: IllegalArgumentException) {
        IllegalArgumentException("unknown color $this")
            .printStackTrace()
        fallbackColor
    }
}
