package app.inspiry.core.log

import android.util.Log

actual object PlatformLogger {

    actual fun verbose(tag: String, message: String) {
        Log.v(tag, message)
    }

    actual fun debug(tag: String, message: String) {
        Log.d(tag, message)
    }

    actual fun info(tag: String, message: String) {
        Log.i(tag, message)
    }

    actual fun warning(tag: String, message: String) {
        Log.w(tag, message)
    }

    actual fun error(tag: String, t: Throwable?, message: String?) {
        Log.e(tag, message, t)
    }
}