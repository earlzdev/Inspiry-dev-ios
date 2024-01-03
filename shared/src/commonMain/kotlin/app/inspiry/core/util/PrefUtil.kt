package app.inspiry.core.util

import com.russhwolf.settings.Settings
import kotlin.reflect.KProperty

class BoolPref(
    private val prefs: Settings,
    private val key: String,
    defaultValue: Boolean
) {

    private var cachedValue = prefs.getBoolean(key, defaultValue)

    operator fun getValue(thisRef: Any?, prop: KProperty<*>) = cachedValue
    operator fun setValue(thisRef: Any?, prop: KProperty<*>, value: Boolean) {
        cachedValue = value
        prefs.putBoolean(key, value)
    }
}