package app.inspiry.core.data

import app.inspiry.core.analytics.putInt
import app.inspiry.core.analytics.putString
import app.inspiry.core.util.getFileName
import app.inspiry.core.util.removeExt
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class OriginalTemplateData(
    val originalCategory: String,
    val originalIndexInCategory: Int,
    val originalPath: String,
) : Parcelable {

    val originalName: String
        get() = originalPath.getFileName().removeExt()

    fun toBundleAnalytics(map: MutableMap<String, Any?>) {
        map.run {
            putString("template_name", originalName)
            putString("template_category", originalCategory)
            putInt("template_index", originalIndexInCategory)
        }
    }
}