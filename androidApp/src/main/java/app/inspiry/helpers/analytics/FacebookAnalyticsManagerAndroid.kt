package app.inspiry.helpers.analytics

import android.content.Context
import android.os.Bundle
import app.inspiry.core.analytics.FacebookAnalyticsManager
import com.facebook.appevents.AppEventsConstants
import com.facebook.appevents.AppEventsLogger

// facebook analytics was shut down by facebook, so it only makes sense to send specific events for better targeting.
class FacebookAnalyticsManagerAndroid(val context: Context) : FacebookAnalyticsManager() {

    val logger = AppEventsLogger.newLogger(context)

    fun getInstance() = logger

    override fun sendFacebookViewContentEvent(name: String) {
        val params = Bundle()
        params.putString(AppEventsConstants.EVENT_PARAM_CONTENT_ID, name)
        logger.logEvent(AppEventsConstants.EVENT_NAME_VIEWED_CONTENT, params)
    }

    companion object {
        fun mapToBundle(map: Map<String, Any?>): Bundle {
            val bundle = Bundle()

            map.forEach {
                val value = it.value
                if (value != null) {
                    val key = it.key
                    when (value) {
                        is Int -> {
                            bundle.putInt(key, value)
                        }
                        is Long -> {
                            bundle.putLong(key, value)
                        }
                        is Float -> {
                            bundle.putFloat(key, value)
                        }
                        is Double -> {
                            bundle.putDouble(key, value)
                        }
                        is String -> {
                            bundle.putString(key, value)
                        }
                        is Boolean -> {
                            bundle.putBoolean(key, value)
                        }
                        else -> throw IllegalStateException("unknown type of $value")
                    }
                }
            }
            return bundle
        }
    }
}

