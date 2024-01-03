package app.inspiry.utils

import android.widget.Toast
import app.inspiry.BuildConfig
import app.inspiry.ap
import com.google.firebase.crashlytics.FirebaseCrashlytics

const val TAG_CODEC = "codec"
const val TAG_TEMPLATE = "template"

fun Throwable.printDebug() {
    if (BuildConfig.DEBUG) printStackTrace()
}

fun Throwable.printCrashlytics() {
    if (BuildConfig.DEBUG) printStackTrace()
    else FirebaseCrashlytics.getInstance().recordException(this)
}

fun Throwable.toastError() {
    Toast.makeText(ap, "Error has happened: $message", Toast.LENGTH_SHORT).show()
}