package app.inspiry.helpers

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.manager.*
import com.adapty.Adapty
import com.adapty.models.PeriodUnit
import com.russhwolf.settings.Settings
import com.russhwolf.settings.contains

class LicenseManagerImplAndroid(
    prefPurchases: Settings,
    remoteConfig: InspRemoteConfig,
    alwaysFreeVersion: Boolean,
    analyticsManager: AnalyticsManager
) : LicenseManagerImpl(prefPurchases, remoteConfig, alwaysFreeVersion, analyticsManager) {

    override fun restorePurchases(forceUpdate: Boolean) {

        Adapty.getPurchaserInfo(forceUpdate) { purchaserInfo, error ->
            error.logError()

            K.d("adapty") {
                "customerUserId ${purchaserInfo?.customerUserId}, restoredPurchases ${purchaserInfo?.accessLevels}," +
                        " isActive ${purchaserInfo?.accessLevels?.get(DEFAULT_ADAPTY_ACCESS)?.isActive}"
            }

            if (error == null && purchaserInfo != null) {

                val accessLevel = purchaserInfo.accessLevels[DEFAULT_ADAPTY_ACCESS]

                if (accessLevel?.isActive != null) {
                    val hasPremium = accessLevel.isActive

                    onGotPurchasesResultRemote(hasPremium, accessLevel.vendorProductId)
                }
            }
        }
    }

    override fun updateTrialDays() {

        if (!prefPurchases.contains(KEY_TRIAL_YEAR_DAYS)) {

            val defaultPaywall =
                remoteConfig.getString("default_adapty_paywall")

            Adapty.getPaywalls { paywalls, products, error ->
                error.logError()
                if (error == null && paywalls != null) {
                    val paywall = paywalls.firstOrNull { it.developerId == defaultPaywall }
                        ?: paywalls.firstOrNull()

                    val productYear =
                        paywall?.products?.firstOrNull { it.subscriptionPeriod?.unit == PeriodUnit.Y }
                    val trial = productYear?.skuDetails?.freeTrialPeriod.extractTrialDays(
                        DEFAULT_TRIAL_YEAR_DAYS
                    )

                    prefPurchases.putInt(KEY_TRIAL_YEAR_DAYS, trial)
                    displayTrialDays.value = trial

                }
            }
        }
    }
}