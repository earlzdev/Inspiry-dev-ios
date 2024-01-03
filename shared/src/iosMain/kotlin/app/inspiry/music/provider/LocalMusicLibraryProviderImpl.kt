package app.inspiry.music.provider

import app.inspiry.music.model.Album
import app.inspiry.music.model.AlbumsResponse
import app.inspiry.music.model.Track
import app.inspiry.music.model.TracksResponse
import platform.MediaPlayer.MPMediaItem
import platform.MediaPlayer.MPMediaQuery

actual class LocalMusicLibraryProviderImpl: LocalMusicLibraryProvider {

    override suspend fun getAlbums(): AlbumsResponse {
        val albums =
        (MPMediaQuery.albumsQuery().items as List<MPMediaItem>).map {
            Album(it.albumPersistentID.toLong(), it.albumTitle ?: "",
                it.albumArtist, it.albumTrackCount.toInt(), null)

        }
        return AlbumsResponse(albums)
    }

    override suspend fun getTracks(albumId: Long): TracksResponse {
        val tracks =
            (MPMediaQuery.songsQuery().items as List<MPMediaItem>).map {
                Track(it.assetURL?.absoluteString ?: "", it.title ?: "", it.artist ?: "", null)

            }

        return TracksResponse(Album(0, "All", "All", tracks.size, ""), tracks)
    }
}