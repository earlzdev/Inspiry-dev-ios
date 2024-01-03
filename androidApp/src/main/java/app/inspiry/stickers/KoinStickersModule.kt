package app.inspiry.stickers

import app.inspiry.stickers.providers.StickersProvider
import app.inspiry.stickers.providers.StickersProviderImpl
import org.koin.dsl.module

object KoinStickersModule {
    fun getModule() = module {
        factory<StickersProvider> { StickersProviderImpl(get(), get()) }
    }
}