package app.inspiry.core.notification

import app.inspiry.bfpromo.BFPromoManager
import app.inspiry.core.manager.InspRemoteConfig
import app.inspiry.core.manager.LicenseManager
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineScope

class DiscountNotificationManager(settings: Settings,
                                  licenseManager: LicenseManager, scope: CoroutineScope,
                                  notificationScheduler: NotificationScheduler,
                                  val bfPromoManager: BFPromoManager
): OneTimePromoNotificationManager(settings, licenseManager, scope, notificationScheduler) {

    override val InspRemoteConfig.sendNotificationAfterDays: Double
        get() = discountNotificationAfterDays


    override fun getType(): NotificationType = NotificationType.DISCOUNT

    override fun additionalShouldSendCondition(): Boolean {
        return bfPromoManager.getBannerDisplayDate() != null && super.additionalShouldSendCondition()
    }
}