package app.inspiry.music.model

import kotlinx.serialization.Serializable

@Serializable
class TracksResponse(val album: Album, val tracks: List<Track>) {
    override fun toString(): String {
        return "TracksResponse(album=$album, tracks=$tracks)"
    }
}