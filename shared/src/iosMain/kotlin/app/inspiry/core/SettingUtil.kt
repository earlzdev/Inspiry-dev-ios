package app.inspiry.core

import com.russhwolf.settings.AppleSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

object SettingUtil {
    fun getAppleSettings(): Settings {
        val delegate = NSUserDefaults("default_user")
        return AppleSettings(delegate)
    }
}