package app.inspiry.music.provider

import app.inspiry.music.model.AlbumsResponse
import app.inspiry.music.model.CacheResponse
import app.inspiry.music.model.MusicTab
import app.inspiry.music.model.TracksResponse

interface MusicLibraryProvider {

    suspend fun getAlbumsCache(): CacheResponse<AlbumsResponse>
    suspend fun getAlbums(): AlbumsResponse

    //if albumId is -1 then return the first item that needs to be displayed
    suspend fun getTracksCache(albumId: Long = -1): CacheResponse<TracksResponse>
    suspend fun getTracks(albumId: Long = -1): TracksResponse
    val supportsCache: Boolean
    val tab: MusicTab
}