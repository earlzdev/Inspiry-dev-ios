package app.inspiry.core.data

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize

@Parcelize
data class Size(val width: Int, val height: Int): Parcelable {
    override fun toString(): String {
        return "Size(width=$width, height=$height)"
    }
}