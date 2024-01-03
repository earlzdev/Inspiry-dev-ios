package app.inspiry.music.util

import app.inspiry.core.util.closeQuietly
import app.inspiry.music.model.Track
import io.ktor.client.call.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okio.BufferedSink
import okio.FileSystem
import okio.Path
import okio.buffer

object TrackUtils {

    fun convertTimeToString(durationMs: Long): String {
        val builder = StringBuilder()
        val secs = durationMs / 1000

        val hours = secs / 3600
        val minutes = secs % 3600 / 60
        val seconds = secs % 60

        if (hours > 0) builder.append(hours).append(":")
        if (minutes < 10) builder.append("0")
        builder.append(minutes)
        builder.append(":")
        if (seconds < 10) builder.append("0")
        builder.append(seconds)

        return builder.toString()
    }

    fun filterTracks(tracks: List<Track>, query: String): List<Track> {
        if (query.isBlank()) return tracks

        return tracks.filter {
            it.artist.contains(query, ignoreCase = true) ||
                    it.title.contains(query, ignoreCase = true)
        }
    }

    fun findSelectedTrackIndex(
        actualTracks: List<Track>,
        initialTitle: String?,
        initialArtist: String?
    ): Int {

        var index = -1
        if (initialArtist != null && initialTitle != null) {
            index =
                actualTracks.indexOfFirst { it.title == initialTitle && it.artist == initialArtist }
        }

        return index
    }


    fun downloadToFile(file: Path, fileSystem: FileSystem, getStatement: suspend () -> HttpStatement): Flow<Float> {
        return flow {
            val statement = getStatement()
            statement.execute { response: HttpResponse ->
                // Response is not downloaded here.

                val limit: Long = Long.MAX_VALUE
                val buffer = ByteArray(4096)

                var out: BufferedSink? = null

                try {

                    val channel = response.receive<ByteReadChannel>()

                    out = fileSystem.appendingSink(file).buffer()

                    val size = response.contentLength()

                    var copied = 0L
                    val bufferSize = buffer.size.toLong()

                    while (copied < limit) {
                        val rc = channel.readAvailable(
                            buffer,
                            0,
                            minOf(limit - copied, bufferSize).toInt()
                        )
                        if (rc == -1) break
                        if (rc > 0) {
                            out.write(buffer, 0, rc)
                            copied += rc

                            if (size != null && size != 0L) {
                                val progress = (copied.toDouble() / size).toFloat()
                                emit(progress)
                            }
                        }
                    }

                } catch (e: Exception) {
                    fileSystem.delete(file)

                    throw e
                } finally {
                    out?.closeQuietly()
                }

                if (response.status.isSuccess()) {

                } else {
                    throw IllegalStateException("file not downloaded")
                }
            }
        }
    }
}