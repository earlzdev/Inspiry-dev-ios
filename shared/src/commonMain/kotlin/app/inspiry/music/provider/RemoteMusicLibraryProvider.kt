package app.inspiry.music.provider

import app.inspiry.music.client.JsonCacheClient
import app.inspiry.music.model.Album
import app.inspiry.music.model.AlbumsResponse
import app.inspiry.music.model.CacheResponse
import app.inspiry.music.model.TracksResponse
import app.inspiry.core.log.KLogger
import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.util.getFileName
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json


abstract class RemoteMusicLibraryProvider(
    private val urlAlbums: String,
    private val urlTracks: String,
    private val httpClient: HttpClient, private val cacheClient: JsonCacheClient,
    private val json: Json, loggerGetter: LoggerGetter
) :
    MusicLibraryProvider {

    val logg: KLogger = loggerGetter.getLogger("RemoteMusicLibraryProvider")

    private fun getCachePath(url: String, args: String = ""): String {
        return "music/${url.getFileName()}${args}.json"
    }

    override suspend fun getAlbumsCache(): CacheResponse<AlbumsResponse> {
        val cachePath = getCachePath(urlAlbums)
        val cache = cacheClient.readCache(cachePath)
        val data: List<Album> = json.decodeFromString(cache)

        return CacheResponse(AlbumsResponse(data), cacheClient.cacheLastModifTime(cachePath))
    }

    override suspend fun getAlbums(): AlbumsResponse {
        val str: String = httpClient.get(urlAlbums)

        try {
            cacheClient.saveCache(getCachePath(urlAlbums), str)
        } catch (e: Exception) {
            logg.error(e)
        }

        return AlbumsResponse(json.decodeFromString(str))
    }

    override suspend fun getTracksCache(albumId: Long): CacheResponse<TracksResponse> {
        val cachePath = getCachePath(urlTracks, "/${albumId}")
        val cache = cacheClient.readCache(cachePath)
        val data: TracksResponse = json.decodeFromString(cache)
        return CacheResponse(data, cacheClient.cacheLastModifTime(cachePath))
    }

    override suspend fun getTracks(albumId: Long): TracksResponse {

        val str: String = httpClient.get(urlTracks) {
            parameter("albumId", albumId)
        }
        try {
            cacheClient.saveCache(getCachePath(urlTracks, "/${albumId}"), str)
        } catch (e: Exception) {
            logg.error(e)
        }

        return json.decodeFromString(str)
    }

    override val supportsCache: Boolean
        get() = true
}