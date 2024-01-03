package app.inspiry.media

import android.widget.TextView
import android.widget.Toast
import app.inspiry.core.media.MediaText
import app.inspiry.core.media.TextAlign
import app.inspiry.font.helpers.PlatformFontObtainerImpl
import app.inspiry.font.helpers.TypefaceObtainingException
import app.inspiry.utils.printDebug
import app.inspiry.utils.toGravity

fun MediaText.setToTextExceptSize(
    textView: TextView,
    platformFontObtainerImpl: PlatformFontObtainerImpl
) {
    textView.gravity = if (!isCircularText()) innerGravity.toGravity() else TextAlign.center.toGravity()
    textView.setTextColor(textColor)

    try {
        textView.typeface =
            platformFontObtainerImpl.getTypefaceFromFontData(font)
    } catch (e: TypefaceObtainingException) {
        e.printDebug()
        Toast.makeText(textView.context, "Cant load typeface ${font?.fontPath}", Toast.LENGTH_LONG).show()
        font = null
    }
    if (!isCircularText()) textView.letterSpacing = letterSpacing
    textView.setLineSpacing(0f, lineSpacing)
    textView.text = text
}