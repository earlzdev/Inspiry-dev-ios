package app.inspiry.music.model

import kotlinx.serialization.Serializable

@Serializable
data class Track(
    val url: String,
    val title: String,
    val artist: String,
    var image: String? = null
) {
    override fun toString(): String {
        return "Track(url='$url', title='$title', artist='$artist', image=$image)"
    }
}