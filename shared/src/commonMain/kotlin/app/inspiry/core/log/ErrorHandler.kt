package app.inspiry.core.log

import app.inspiry.core.manager.DebugManager
import app.inspiry.core.manager.ToastManager

abstract class ErrorHandler(val toastManager: ToastManager) {
    abstract fun recordFirebaseException(t: Throwable)

    fun printCrashlytics(t: Throwable) {
        if (DebugManager.isDebug) t.printStackTrace()
        else recordFirebaseException(t)
    }

    // TODO: maybe localize
    fun toastError(t: Throwable, onlyMessage: Boolean = true) {
        val message = t.message
        if (DebugManager.isDebug)
            t.printStackTrace()
        if (!message.isNullOrBlank()) {
            if (onlyMessage) {
                toastManager.displayToast(message)
            } else {
                toastManager.displayToast("Error has happened: ${message}")
            }
        }
    }
}