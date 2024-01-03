package app.inspiry.utils

import android.content.Context
import java.lang.Exception
import java.lang.IllegalStateException
import java.util.*

/**
 * Replacement for Kotlin's deprecated `capitalize()` function.
 */
fun String.capitalized(locale: Locale = Locale.getDefault()): String {
    return this.replaceFirstChar {
        if (it.isLowerCase())
            it.titlecase(locale)
        else it.toString()
    }
}

/**
 * Get drawable from name
 * @return Drawable resource id ( e.g. val drawable = "ic_edit_icon".getDrawable(context) )
 */
fun String.getDrawable(context: Context): Int {
    val res =  context.resources.getIdentifier(this, "drawable", context.packageName)
    if (res == 0 )  throw IllegalStateException("resource $this not loaded")
    return res

}