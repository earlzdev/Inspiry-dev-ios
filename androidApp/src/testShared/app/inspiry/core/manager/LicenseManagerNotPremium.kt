package app.inspiry.core.manager

import app.inspiry.core.analytics.AnalyticsManager
import com.russhwolf.settings.Settings

class LicenseManagerNotPremium(
    prefPurchases: Settings,
    remoteConfig: InspRemoteConfig,
    alwaysFreeVersion: Boolean,
    analyticsManager: AnalyticsManager
) :
    LicenseManagerImpl(prefPurchases, remoteConfig, alwaysFreeVersion, analyticsManager) {


    override fun restorePurchases(forceUpdate: Boolean) {

    }

    override fun updateTrialDays() {
    }
}