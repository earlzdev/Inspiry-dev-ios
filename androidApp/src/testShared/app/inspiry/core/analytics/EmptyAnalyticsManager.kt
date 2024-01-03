package app.inspiry.core.analytics

class EmptyAnalyticsManager: AnalyticsManager {

    override fun sendEvent(
        eventName: String,
        outOfSession: Boolean,
        createParams: (MutableMap<String, Any?>.() -> Unit)?
    ) {

    }

    override fun setUserProperty(name: String, value: String) {
    }
}