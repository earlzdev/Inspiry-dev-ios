package app.inspiry.core.manager

import kotlin.jvm.JvmOverloads

interface ToastManager {
    fun displayToast(text: String, length: ToastLength = ToastLength.SHORT)
}

enum class ToastLength(val seconds: Int) {
    SHORT(3), LONG(6)
}