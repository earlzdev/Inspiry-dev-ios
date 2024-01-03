package app.inspiry.core

import app.inspiry.core.analytics.CommonAnalyticsManager
import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.manager.InstagramSubscribeHolder
import app.inspiry.core.manager.InstagramSubscribeHolderImpl
import org.koin.dsl.module

object KoinCommonModule {
    fun getModule() = module {
        single<AnalyticsManager> { CommonAnalyticsManager(get(), get(), get(), get(), get(), get()) }
        single<InstagramSubscribeHolder> {
            InstagramSubscribeHolderImpl(get())
        }
    }
}
