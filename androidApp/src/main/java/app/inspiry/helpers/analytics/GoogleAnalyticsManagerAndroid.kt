package app.inspiry.helpers.analytics

import android.content.Context
import android.os.Bundle
import app.inspiry.BuildConfig
import app.inspiry.core.analytics.GoogleAnalyticsManager
import com.google.firebase.analytics.FirebaseAnalytics

class GoogleAnalyticsManagerAndroid(val context: Context) : GoogleAnalyticsManager() {

    init {
        FirebaseAnalytics.getInstance(context).setAnalyticsCollectionEnabled(!BuildConfig.DEBUG)
    }

    override fun sendEvent(
        eventName: String,
        outOfSession: Boolean,
        createParams: (MutableMap<String, Any?>.() -> Unit)?
    ) {
        var args: Bundle? = null
        if (createParams != null) {
            val map = mutableMapOf<String, Any?>()
            map.createParams()
            args = FacebookAnalyticsManagerAndroid.mapToBundle(map)
        }
        FirebaseAnalytics.getInstance(context).logEvent(eventName, args)
    }

    override fun setUserProperty(name: String, value: String) {
        FirebaseAnalytics.getInstance(context).setUserProperty(name, value)
    }
}