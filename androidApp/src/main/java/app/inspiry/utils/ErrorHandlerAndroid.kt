package app.inspiry.utils

import app.inspiry.core.log.ErrorHandler
import app.inspiry.core.manager.ToastManager
import com.google.firebase.crashlytics.FirebaseCrashlytics

class ErrorHandlerAndroid(toastManager: ToastManager): ErrorHandler(toastManager) {
    override fun recordFirebaseException(t: Throwable) {
        FirebaseCrashlytics.getInstance().recordException(t)
    }
}