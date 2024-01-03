package app.inspiry.export

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize

private const val GALLERY = "gallery"
private const val MORE = "more"

@Parcelize
data class WhereToExport(val whereApp: String, val whereScreen: String): Parcelable {
    constructor(whereApp: String): this(whereApp, whereApp)
}

val whereToExportGallery = WhereToExport(GALLERY, GALLERY)
val whereToExportMore = WhereToExport(MORE, MORE)