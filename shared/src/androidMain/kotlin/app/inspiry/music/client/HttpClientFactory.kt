package app.inspiry.music.client

import app.inspiry.core.log.LoggerGetter
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import kotlinx.serialization.json.Json

actual object HttpClientFactory {

    actual fun createHttpClient(json: Json, loggerGetter: LoggerGetter): HttpClient {

        return HttpClient(OkHttp) {
            install(JsonFeature) {
                serializer = KotlinxSerializer(json)
            }

            install(Logging) {
                logger = object : Logger {
                    private val logg = loggerGetter.getLogger("ktor")
                    override fun log(message: String) {
                        logg.debug { message }
                    }
                }
                level = LogLevel.ALL
            }

            install(HttpTimeout) {
                requestTimeoutMillis = 30000L
                connectTimeoutMillis = 15000L
                socketTimeoutMillis = 15000L
            }

            /*
            defaultRequest {

                // Content Type
                if (this.method != HttpMethod.Get) contentType(ContentType.Application.Json)
                accept(ContentType.Application.Json)
            }*/
        }
    }
}