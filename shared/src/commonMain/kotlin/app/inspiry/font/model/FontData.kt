package app.inspiry.font.model

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * fontPath:
 * 1. null - roboto
 * 2. id of predefined font
 * 3. local path with font (without styles)
 */
@Parcelize
@Serializable
data class FontData(
    var fontPath: String? = null,
    val fontStyle: InspFontStyle? = InspFontStyle.regular
) : Parcelable
