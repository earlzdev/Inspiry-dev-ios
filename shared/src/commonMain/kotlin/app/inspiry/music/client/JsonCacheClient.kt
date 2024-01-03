package app.inspiry.music.client

interface JsonCacheClient {

    @Throws(Exception::class)
    fun saveCache(path: String, cache: String)

    @Throws(Exception::class)
    fun readCache(path: String): String

    fun cacheLastModifTime(path: String): Long

    fun deleteCache(path: String)

    companion object {
        const val JSON_CACHE_FILE_NAME = "json_cache"
    }
}
