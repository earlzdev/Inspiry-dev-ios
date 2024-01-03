package app.inspiry.music.client

import app.inspiry.music.provider.ITunesMusicLibraryProvider
import app.inspiry.music.provider.RemoteLibraryMusicProvider
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*

object MockHttpClientFactory {
    private const val ALBUMS_RESPONSE = "[\n" +
            "    {\n" +
            "      \"id\": 1,\n" +
            "      \"name\": \"Lo Fi\",\n" +
            "      \"tracksCount\": 12,\n" +
            "      \"artist\": \"Youtuber\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": 2,\n" +
            "      \"name\": \"Lifestyle\",\n" +
            "      \"tracksCount\": 15,\n" +
            "      \"artist\": \"Musician\"\n" +
            "    }\n" +
            "  ]"
    private const val TRACKS_RESPONSE = "{\n" +
            "  \"album\": {\n" +
            "    \"id\": 1,\n" +
            "    \"name\": \"Lo Fi\",\n" +
            "    \"tracksCount\": 15,\n" +
            "    \"artist\": \"Musician\"\n" +
            "  },\n" +
            "  \"tracks\": [\n" +
            "    {\n" +
            "      \"url\": \"https://some_url.mp3\",\n" +
            "      \"title\": \"Chill Beat\",\n" +
            "      \"artist\": \"Youtuber\",\n" +
            "      \"duration\": 30\n" +
            "    }\n" +
            "  ]\n" +
            "}"

    fun createMockHttpClient() = HttpClient(MockEngine) {
        engine {
            addHandler { request ->

                val responseHeaders =
                    headersOf("Content-Type" to listOf(ContentType.Text.Plain.toString()))

                val fullUrl = request.url.fullUrl
                if (fullUrl.startsWith(RemoteLibraryMusicProvider.URL_GET_ALBUMS) ||
                    fullUrl.startsWith(ITunesMusicLibraryProvider.URL_GET_ALBUMS)
                ) {
                    respond(ALBUMS_RESPONSE, headers = responseHeaders)
                } else if (fullUrl.startsWith(RemoteLibraryMusicProvider.URL_GET_TRACKS) ||
                    fullUrl.startsWith(ITunesMusicLibraryProvider.URL_GET_TRACKS)
                ) {
                    respond(TRACKS_RESPONSE, headers = responseHeaders)
                } else {
                    error("Unhandled ${request.url.fullUrl}")
                }
            }
        }
    }

    private val Url.hostWithPortIfRequired: String get() = if (port == protocol.defaultPort) host else hostWithPort
    private val Url.fullUrl: String get() = "${protocol.name}://$hostWithPortIfRequired$fullPath"
}