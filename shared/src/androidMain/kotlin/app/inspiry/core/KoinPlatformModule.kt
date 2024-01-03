package app.inspiry.core

import android.content.Context
import com.russhwolf.settings.AndroidSettings
import org.koin.dsl.module

object KoinPlatformModule {
    fun getModule() = module {

        single<com.russhwolf.settings.Settings> {

            val context: Context = get()
            val pref = context.getSharedPreferences("arbitrary_prefs", Context.MODE_PRIVATE)
            AndroidSettings(pref)
        }
    }
}