package app.inspiry.core.manager

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

class CommonClipboardManagerImpl(val context: Context): CommonClipBoardManager {

    override fun copyToClipboard(text: String) {
        text.copyToClipboard(context)
    }
}

fun String.copyToClipboard(context: Context) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Copied Text", this)
    clipboard.setPrimaryClip(clip)
}