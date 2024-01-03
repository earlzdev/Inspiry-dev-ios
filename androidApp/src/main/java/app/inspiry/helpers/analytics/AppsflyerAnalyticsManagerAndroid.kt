package app.inspiry.helpers.analytics

import android.content.Context
import app.inspiry.core.analytics.AppsflyerAnalyticsManager

class AppsflyerAnalyticsManagerAndroid(val context: Context): AppsflyerAnalyticsManager() {
    override fun sendEvent(
        eventName: String,
        outOfSession: Boolean,
        createParams: (MutableMap<String, Any?>.() -> Unit)?
    ) {
        TODO("Not yet implemented")
    }

    override fun setUserProperty(name: String, value: String) {
        TODO("Not yet implemented")
    }
}