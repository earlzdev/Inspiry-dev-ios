package app.inspiry.music.model

import kotlinx.serialization.Serializable

//this wrapper is used only for objective c / swift interpolation, because Response<List<Album>> doesn't work
@Serializable
class AlbumsResponse(val albums: List<Album>) {
    override fun toString(): String {
        return "AlbumsResponse(albums=$albums)"
    }
}