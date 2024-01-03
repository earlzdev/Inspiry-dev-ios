package app.inspiry.core.media

import kotlinx.serialization.Serializable

@Serializable
data class LayoutPosition(
    var width: String = MATCH_PARENT,
    var height: String = MATCH_PARENT,
    var alignBy: Alignment = Alignment.top_start,
    var x: String? = null,
    var y: String? = null,
    var paddingEnd: String? = null,
    var paddingBottom: String? = null,
    var paddingStart: String? = null,
    var paddingTop: String? = null,
    var marginRight: String? = null,
    var marginBottom: String? = null,
    var marginLeft: String? = null,
    var marginTop: String? = null,

    //true - calculate values relative to parent group otherwise to the root InspTemplateView size.
    val relativeToParent: Boolean = true
) {
    fun isInWrapContentMode() = width == WRAP_CONTENT && height == WRAP_CONTENT
    companion object {
        const val TAKE_FROM_MEDIA = "take_from_media"
        const val MATCH_PARENT = "match_parent"
        const val WRAP_CONTENT = "wrap_content"
    }
}
