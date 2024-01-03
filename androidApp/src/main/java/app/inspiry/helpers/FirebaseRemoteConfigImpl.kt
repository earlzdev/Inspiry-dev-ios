package app.inspiry.helpers

import app.inspiry.BuildConfig
import app.inspiry.R
import app.inspiry.core.manager.InspRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

class FirebaseRemoteConfigImpl : InspRemoteConfig() {

    init {
        val remoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else minimumFetchInterval.toLong())
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)

        val task = remoteConfig.fetchAndActivate()

        task.addOnCompleteListener {
            // do we need to check if it.isSuccessful ?
            onActivated()
        }
    }

    override fun getBoolean(key: String): Boolean {
        return FirebaseRemoteConfig.getInstance().getBoolean(key)
    }

    override fun getString(key: String): String {
        return FirebaseRemoteConfig.getInstance().getString(key)
    }

    override fun getDouble(key: String): Double {
        return FirebaseRemoteConfig.getInstance().getDouble(key)
    }

    override fun getLong(key: String): Long {
        return FirebaseRemoteConfig.getInstance().getLong(key)
    }
}