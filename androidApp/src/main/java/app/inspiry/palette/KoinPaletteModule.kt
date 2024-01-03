package app.inspiry.palette

import app.inspiry.palette.provider.PaletteProvider
import app.inspiry.palette.provider.PaletteProviderImpl
import org.koin.dsl.module

object KoinPaletteModule {
    fun getModule() = module {
        factory<PaletteProvider> {
            PaletteProviderImpl()
        }
    }
}