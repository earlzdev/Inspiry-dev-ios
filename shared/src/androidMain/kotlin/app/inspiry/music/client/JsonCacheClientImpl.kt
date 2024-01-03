package app.inspiry.music.client

import android.content.Context
import app.inspiry.music.client.JsonCacheClient.Companion.JSON_CACHE_FILE_NAME
import app.inspiry.core.util.writeToStream
import okio.buffer
import okio.source
import java.io.File

class JsonCacheClientImpl(val context: Context) : JsonCacheClient {

    private val cachePath = File(context.cacheDir, JSON_CACHE_FILE_NAME)

    @Throws(Exception::class)
    override fun saveCache(path: String, cache: String) {
        val file = File(cachePath, path)
        file.parentFile?.mkdirs()

        cache.writeToStream(file.outputStream())
    }

    @Throws(Exception::class)
    override fun readCache(path: String): String {
        val file = File(cachePath, path)
        return file.source().buffer().readUtf8()
    }

    override fun cacheLastModifTime(path: String): Long {
        return File(cachePath, path).lastModified()
    }

    override fun deleteCache(path: String) {
        File(cachePath, path).delete()
    }
}