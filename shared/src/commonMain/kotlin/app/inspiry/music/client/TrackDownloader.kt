package app.inspiry.music.client

import app.inspiry.music.util.TrackUtils
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.Flow
import okio.Path

class TrackDownloader(private val httpClient: HttpClient) {

    fun downloadFile(url: String, file: Path, fileSystem: okio.FileSystem): Flow<Float> {
        return TrackUtils.downloadToFile(file, fileSystem) { httpClient.get(url, block = { }) }
    }
}