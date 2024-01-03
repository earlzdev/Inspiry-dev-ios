package app.inspiry.core.util

import com.russhwolf.settings.Settings

inline fun Settings.doOnce(key: String, action: () -> Unit) {
    if (getBoolean(key, true)) {
        action()
        putBoolean(key, false)
    }
}