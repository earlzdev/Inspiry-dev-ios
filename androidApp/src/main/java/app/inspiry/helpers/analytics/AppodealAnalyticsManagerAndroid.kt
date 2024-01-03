package app.inspiry.helpers.analytics

import android.content.Context
import app.inspiry.core.analytics.AppodealAnalyticsManager

class AppodealAnalyticsManagerAndroid(val context: Context): AppodealAnalyticsManager() {
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