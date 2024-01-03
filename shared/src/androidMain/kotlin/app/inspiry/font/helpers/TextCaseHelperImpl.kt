package app.inspiry.font.helpers

import android.content.Context
import androidx.core.os.ConfigurationCompat
import androidx.core.os.LocaleListCompat
import java.util.*

class TextCaseHelperImpl(val context: Context) : TextCaseHelper() {

    override fun toLowerCase(value: String): String {
        return value.lowercase(context.getCurrentLocale())
    }

    override fun toUpperCase(value: String): String {
        return value.uppercase(context.getCurrentLocale())
    }

    override fun capitalize(value: String): String {
        val locale = context.getCurrentLocale()
        return value.lowercase(locale)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }

}

fun Context.getCurrentLocale(): Locale = ConfigurationCompat.getLocales(resources.configuration)[0]