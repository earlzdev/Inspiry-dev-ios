package app.inspiry.core.notification

import app.inspiry.core.manager.InspRemoteConfig
import app.inspiry.core.manager.LicenseManager
import com.russhwolf.settings.Settings
import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import kotlinx.coroutines.*

abstract class OneTimePromoNotificationManager(
    val settings: Settings,
    val licenseManager: LicenseManager,
    val scope: CoroutineScope,
    notificationScheduler: NotificationScheduler
) :
    NotificationManager(notificationScheduler) {

    // -1 - send instantly
    // 0 - send never
    abstract val InspRemoteConfig.sendNotificationAfterDays: Double

    // open to be backwards compatible
    open val notificationSendKey: String
        get() = getType().name + "_is_sent"


    override fun onSendNotification() {
        settings.putBoolean(notificationSendKey, true)
    }

    open fun additionalShouldSendCondition(): Boolean {
        return true
    }

    private var collectLicenseJob: Job? = null
    override fun onRemoteConfigActivated(remoteConfig: InspRemoteConfig) {

        if (settings.getBoolean(notificationSendKey, defaultValue = false))
            return

        val notifyAfterDays: Double = remoteConfig.sendNotificationAfterDays

        if (licenseManager.hasPremiumState.value
            || notifyAfterDays == 0.0 || !additionalShouldSendCondition()
        ) {
            cancelNotification()
            return
        }

        collectLicenseJob?.cancel()
        // start is used to get immediate result and pass test
        collectLicenseJob = scope.launch(start = CoroutineStart.UNDISPATCHED) {

            licenseManager.hasPremiumState.collect {
                onGetIsPremiumData(it, notifyAfterDays)
            }
        }
    }

    private fun onGetIsPremiumData(isPremium: Boolean, notifyAfterDays: Double) {
        if (!isPremium && notifyAfterDays != 0.0 && additionalShouldSendCondition()) {

            if (!notificationScheduler.isNotificationScheduled(getType())) {

                val time =
                    DateTime.nowUnixLong() + (if (notifyAfterDays == -1.0) 0 else notifyAfterDays.days.millisecondsLong)

                notificationScheduler.oneTimeNotificationAt(
                    time,
                    getType()
                )
            }
        } else {
            cancelNotification()
        }
    }
}