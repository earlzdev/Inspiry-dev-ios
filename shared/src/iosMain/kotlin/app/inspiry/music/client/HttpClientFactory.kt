package app.inspiry.music.client

import app.inspiry.core.log.LoggerGetter
import io.ktor.client.*
import io.ktor.client.engine.ios.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.json.Json

actual object HttpClientFactory {

    actual fun createHttpClient(json: Json, loggerGetter: LoggerGetter): HttpClient {
        return HttpClient(Ios) {

            install(JsonFeature) {
                serializer = KotlinxSerializer(json)
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 30000L
                connectTimeoutMillis = 15000L
                socketTimeoutMillis = 15000L
            }
            /*defaultRequest {
                if (this.method != HttpMethod.Get) contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }*/
        }

    }
}