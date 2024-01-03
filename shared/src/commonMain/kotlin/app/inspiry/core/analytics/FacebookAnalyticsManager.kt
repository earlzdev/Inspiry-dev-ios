package app.inspiry.core.analytics

abstract class FacebookAnalyticsManager : AnalyticsManager {
    final override fun sendEvent(
        eventName: String,
        outOfSession: Boolean,
        createParams: (MutableMap<String, Any?>.() -> Unit)?
    ) {
        if (eventName == AnalyticsManager.TEMPLATE_SHARE_ACTION) {

            val p = mutableMapOf<String, Any?>()
            createParams?.let { p.it() }

            val templateId =
                p["template_name"]?.toString() + "_" + p["template_category"]?.toString()
            sendFacebookViewContentEvent(templateId)
        }
    }

    abstract fun sendFacebookViewContentEvent(name: String)

    final override fun setUserProperty(name: String, value: String) {
    }
}