package app.inspiry.font.util

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

fun Uri.getFontName(context: Context): String? {

    val cursor: Cursor? = context.contentResolver.query(
        this,
        null,
        null,
        null,
        null,
        null
    )

    cursor?.use {

        if (it.moveToFirst()) {

            return it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
        }
    }
    return null
}


fun Context.getSavedFontsDir() = File(filesDir, "fonts")

