package app.inspiry.core.manager

import app.inspiry.core.analytics.AmplitudeAnalyticsManager
import app.inspiry.core.notification.*
import app.inspiry.core.template.MyTemplatesViewModel
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class AppViewModel(private val settings: Settings) {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun onCreate(
        notificationManagersContainer: NotificationManagersContainer,
        amplitudeAnalyticsManager: AmplitudeAnalyticsManager, remoteConfig: InspRemoteConfig
    ) {

        remoteConfig.doWhenValuesActivated {

            notificationManagersContainer.list.forEach { it.onRemoteConfigActivated(remoteConfig) }

            // to be able to run and analyze 5 experiments in parallel
            for (i in 1..5) {
                val fbExId = "${FB_EXPERIMENT_ID}_$i"
                val experimentId = remoteConfig.getString(fbExId)
                amplitudeAnalyticsManager.setUserProperty(fbExId, experimentId)
            }
        }
    }

    companion object {
        const val FB_EXPERIMENT_ID = "FirebaseExperimentId"
    }
}