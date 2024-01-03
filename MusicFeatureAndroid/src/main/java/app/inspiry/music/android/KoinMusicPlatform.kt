package app.inspiry.music.android

import android.content.Context
import app.inspiry.music.android.client.BaseAudioStatePlayer
import app.inspiry.music.android.client.ExoAudioStatePlayer
import app.inspiry.music.android.client.MusicFileCreatorJVM
import app.inspiry.music.client.JsonCacheClient
import app.inspiry.music.client.JsonCacheClientImpl
import app.inspiry.music.client.MusicFileCreator
import com.google.android.exoplayer2.database.ExoDatabaseProvider
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.upstream.cache.Cache
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import org.koin.dsl.module
import java.io.File

object KoinMusicPlatform {
    fun getModulePlatform() = module {

        single<Cache> {
            val context: Context = get()
            val cacheFolder = File(context.cacheDir, "music")
            val cacheEvictor = LeastRecentlyUsedCacheEvictor(100 * 1024 * 1024)
            SimpleCache(cacheFolder, cacheEvictor, StandaloneDatabaseProvider(context))
        }

        single<JsonCacheClient> { JsonCacheClientImpl(get()) }

        factory<MusicFileCreator> { MusicFileCreatorJVM(get()) }
    }
}