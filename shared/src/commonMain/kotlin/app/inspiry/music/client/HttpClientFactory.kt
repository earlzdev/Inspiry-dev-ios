package app.inspiry.music.client

import app.inspiry.core.log.LoggerGetter
import io.ktor.client.*
import kotlinx.serialization.json.Json

expect object HttpClientFactory {
    fun createHttpClient(json: Json, loggerGetter: LoggerGetter): HttpClient
}