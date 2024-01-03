package app.inspiry.music

import app.inspiry.music.client.HttpClientFactory
import app.inspiry.music.provider.*
import org.koin.dsl.module

object KoinMusicModule {
    fun getModule() = module {
        factory<LocalMusicLibraryProvider> { LocalMusicLibraryProviderImpl() }

        factory {
            ITunesMusicLibraryProvider(get(), get(), get(), get())
        }
        factory {
            RemoteLibraryMusicProvider(get(), get(), get(), get())
        }

        single { HttpClientFactory.createHttpClient(get(), get()) }
    }
}