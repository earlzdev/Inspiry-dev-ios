package app.inspiry.core.notification

import app.inspiry.core.manager.InspRemoteConfig
import app.inspiry.core.manager.LicenseManager
import app.inspiry.removebg.RemovingBgViewModel
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.addIntOrNullListener
import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * We show notification if user hasn't used remove bg tool even once.
 */
class RemoveBgNotificationManager(
    settings: Settings,
    notificationScheduler: NotificationScheduler,
    licenseManager: LicenseManager,
    scope: CoroutineScope
) : OneTimePromoNotificationManager(settings, licenseManager, scope, notificationScheduler) {

    init {
        (settings as ObservableSettings).addIntOrNullListener(RemovingBgViewModel.KEY_NUM_PROCESSED_IMAGES) {
            if (it != null && it > 0) {
                cancelNotification()
            }
        }
    }

    override fun additionalShouldSendCondition(): Boolean {
        return settings.getInt(RemovingBgViewModel.KEY_NUM_PROCESSED_IMAGES) == 0
    }

    override val InspRemoteConfig.sendNotificationAfterDays: Double
        get() = this.removeBgNotifyAfterDays

    override val notificationSendKey: String
        get() = KEY_REMOVE_BG_NOTIFICATION_SEND

    override fun getType(): NotificationType = NotificationType.REMOVE_BG
}

private const val KEY_REMOVE_BG_NOTIFICATION_SEND = "remove_bg_notification_send"