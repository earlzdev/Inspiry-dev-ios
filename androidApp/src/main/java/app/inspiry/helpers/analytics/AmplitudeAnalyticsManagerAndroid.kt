package app.inspiry.helpers.analytics

import android.app.Application
import android.content.Context
import app.inspiry.BuildConfig
import app.inspiry.core.analytics.AmplitudeAnalyticsManager
import com.amplitude.api.Amplitude
import org.json.JSONObject
import java.util.*

class AmplitudeAnalyticsManagerAndroid(val context: Context) : AmplitudeAnalyticsManager() {

    init {
        Amplitude.getInstance()
            .initialize(context, "amplitude_api_key")
            .enableForegroundTracking(context as Application)
    }

    fun getDeviceId(): String? = Amplitude.getInstance().deviceId
    fun getUserId(): String? = Amplitude.getInstance().userId

    override fun sendEvent(
        eventName: String,
        outOfSession: Boolean,
        createParams: (MutableMap<String, Any?>.() -> Unit)?
    ) {
        if (BuildConfig.DEBUG) return

        var eventProperties: JSONObject? = null
        if (createParams != null) {
            eventProperties = JSONObject()
            val map = HashMap<String, Any?>()
            map.createParams()
            map.forEach {
                eventProperties.put(it.key, it.value?.toString() ?: "none")
            }
        }
        Amplitude.getInstance().logEvent(eventName, eventProperties)
    }

    override fun setUserProperty(name: String, value: String) {
        Amplitude.getInstance().setUserProperties(JSONObject().also { it.put(name, value) })
    }
}