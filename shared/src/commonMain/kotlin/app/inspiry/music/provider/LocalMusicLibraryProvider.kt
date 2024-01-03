package app.inspiry.music.provider

import app.inspiry.music.model.AlbumsResponse
import app.inspiry.music.model.CacheResponse
import app.inspiry.music.model.MusicTab
import app.inspiry.music.model.TracksResponse

interface LocalMusicLibraryProvider : MusicLibraryProvider {
    override val supportsCache: Boolean
        get() = false

    override suspend fun getAlbumsCache(): CacheResponse<AlbumsResponse> {
        throw UnsupportedOperationException()
    }

    override suspend fun getTracksCache(albumId: Long): CacheResponse<TracksResponse> {
        throw UnsupportedOperationException()
    }

    override val tab: MusicTab
        get() = MusicTab.MY_MUSIC
}