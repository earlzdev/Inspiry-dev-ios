package app.inspiry.core.analytics

import app.inspiry.core.log.KLogger
import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.manager.DebugManager

class CommonAnalyticsManager(
    amplitudeAnalyticsManager: AmplitudeAnalyticsManager,
    facebookAnalyticsManager: FacebookAnalyticsManager,
    googleAnalyticsManager: GoogleAnalyticsManager,
    appsflyerAnalyticsManager: AppsflyerAnalyticsManager,
    appodealAnalyticsManager: AppodealAnalyticsManager,
    loggerGetter: LoggerGetter
) : AnalyticsManager {

    private val managers: List<AnalyticsManager> =
        listOf(googleAnalyticsManager, facebookAnalyticsManager, amplitudeAnalyticsManager, appsflyerAnalyticsManager, appodealAnalyticsManager)

    val logger: KLogger = loggerGetter.getLogger("CommonAnalyticsManager")

    override fun sendEvent(
        eventName: String,
        outOfSession: Boolean,
        createParams: (MutableMap<String, Any?>.() -> Unit)?
    ) {
        logger.info {
            "send event $eventName, params ${
                createParams?.let {
                    HashMap<String, Any?>().also {
                        it.createParams()
                        it.toString()
                    }
                }
            }"
        }
        //if (!DebugManager.isDebug) {
            managers.forEach {
                it.sendEvent(eventName, false, createParams)
            }
        //}
    }

    override fun setUserProperty(name: String, value: String) {
        logger.info { "set user property ${name}, ${value}" }
        //if (!DebugManager.isDebug) {
            managers.forEach {
                it.setUserProperty(name, value)
            }
        //}
    }
}
