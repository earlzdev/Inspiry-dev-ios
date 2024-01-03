package app.inspiry.font

import app.inspiry.font.helpers.PlatformFontObtainerImpl
import app.inspiry.font.helpers.TextCaseHelper
import app.inspiry.font.helpers.TextCaseHelperImpl
import app.inspiry.font.provider.*
import app.inspiry.projectutils.BuildConfig
import org.koin.dsl.module

object KoinFontsModule {
    fun getModule() = module {
        factory<UploadedFontsProvider> { UploadedFontsProviderImpl(get()) }
        factory<TextCaseHelper> { TextCaseHelperImpl(get()) }
        factory<PlatformFontPathProvider> { PlatformFontPathProviderImpl() }
        single {
            FontsManager(BuildConfig.DEBUG, get())
        }
        //we don't hide implementation because of generics
        factory { PlatformFontObtainerImpl(get(), get(), get()) }
    }
}