package app.inspiry.core.analytics

import app.inspiry.core.manager.InspRemoteConfig

open class EmptyRemoteConfig: InspRemoteConfig() {
    override fun getBoolean(key: String): Boolean {
        return false
    }

    override fun getString(key: String): String {
        return ""
    }

    override fun getDouble(key: String): Double {
        return 0.0
    }

    override fun getLong(key: String): Long {
        return 0L
    }
}