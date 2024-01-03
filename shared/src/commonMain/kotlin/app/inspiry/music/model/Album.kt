package app.inspiry.music.model

import kotlinx.serialization.Serializable

@Serializable
data class Album(
    val id: Long,
    val name: String,
    val artist: String? = null,
    val tracksCount: Int = 0,
    val image: String? = null
) {
    override fun toString(): String {
        return "Album(id=$id, name='$name', artist='$artist', tracksCount=$tracksCount)"
    }
}