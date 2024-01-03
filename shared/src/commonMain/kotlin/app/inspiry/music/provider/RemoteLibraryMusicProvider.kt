package app.inspiry.music.provider

import app.inspiry.music.client.JsonCacheClient
import app.inspiry.music.model.MusicTab
import app.inspiry.core.log.LoggerGetter
import io.ktor.client.*
import kotlinx.serialization.json.Json

class RemoteLibraryMusicProvider(
    httpClient: HttpClient, cacheClient: JsonCacheClient, json: Json,
    loggerGetter: LoggerGetter
) : RemoteMusicLibraryProvider(
    URL_GET_ALBUMS, URL_GET_TRACKS,
    httpClient,
    cacheClient,
    json, loggerGetter
) {

    companion object {
        const val URL_GET_ALBUMS =
            "https://api.music.host/music/getLibraryAlbums"
        const val URL_GET_TRACKS =
            "https://api.music.host/music/getLibraryTracks"
    }

    override val tab: MusicTab
        get() = MusicTab.LIBRARY
}