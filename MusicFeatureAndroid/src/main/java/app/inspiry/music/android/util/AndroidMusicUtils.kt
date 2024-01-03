package app.inspiry.music.android.util

import android.content.Context
import android.provider.MediaStore

fun String?.localizedUnknownArtist(context: Context): String {
    if (this == MediaStore.UNKNOWN_STRING || this.isNullOrBlank())
        return context.getString(app.inspiry.projectutils.R.string.music_unknown_artist)
    else return this
}

fun String?.isUnknownArtist(): Boolean {
    return this == MediaStore.UNKNOWN_STRING || this.isNullOrBlank()
}