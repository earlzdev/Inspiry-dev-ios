package app.inspiry.core.manager

import app.inspiry.core.analytics.AnalyticsManager
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.MutableStateFlow

abstract class LicenseManagerImpl(
    val prefPurchases: Settings,
    val remoteConfig: InspRemoteConfig,
    var alwaysFreeVersion: Boolean,
    val analyticsManager: AnalyticsManager
) : LicenseManager {

    override val hasPremiumState: MutableStateFlow<Boolean> =
        MutableStateFlow(hasAllInclusive())

    override val displayTrialDays: MutableStateFlow<Int> =
        MutableStateFlow(prefPurchases.getInt(KEY_TRIAL_YEAR_DAYS, DEFAULT_TRIAL_YEAR_DAYS))

    private val regexTrialDays by lazy { Regex("P(\\d*)D") }

    private fun hasAllInclusive() = !alwaysFreeVersion && (hasAllInclusivePrefs() ||
            remoteConfig.getBoolean("free_cause_payments_failed"))

    private fun hasAllInclusivePrefs() = prefPurchases.getBoolean(KEY_HAS_PREMIUM, false)

    override fun debugActivatePurchase() {
        alwaysFreeVersion = false
        onPurchaseActivated("debug_id")
    }

    override fun mayShowFreeDialogCausePaymentsFailed(): Boolean {
        return !hasAllInclusivePrefs() &&
                remoteConfig.getBoolean("free_cause_payments_failed") &&
                remoteConfig.getBoolean("show_dialog_free_cause_payments_failed")
    }

    override fun onPurchaseActivated(id: String) {
        if (alwaysFreeVersion) {
            onPurchaseDeactivated()
            return
        }

        setAnalyticsId(id)
        prefPurchases.putBoolean(KEY_HAS_PREMIUM, true)
        hasPremiumState.value = true
    }

    private fun setAnalyticsId(productId: String) {
        analyticsManager.setUserProperty("all_inclusive_license", productId)
    }

    private fun onPurchaseDeactivated() {
        prefPurchases.putBoolean(KEY_HAS_PREMIUM, false)
        hasPremiumState.value = false
        setAnalyticsId("")
    }

    protected fun onGotPurchasesResultRemote(hasPremium: Boolean, id: String) {
        val hadPremium = hasAllInclusive()

        if (hasPremium != hadPremium) {
            if (hasPremium) {
                onPurchaseActivated(id)
            } else {
                onPurchaseDeactivated()
            }
        }
        // to set analyticsId for existing users.
        else if (hasPremium)
            setAnalyticsId(id)
    }

    fun String?.extractTrialDays(defaultValue: Int): Int {
        if (this == null) return defaultValue
        val regexRes = regexTrialDays.find(this)

        val trialDaysMatch = regexRes
            ?.groupValues?.getOrNull(1)

        return if (trialDaysMatch == null) defaultValue else try {
            trialDaysMatch.toInt()
        } catch (e: Exception) {
            defaultValue
        }
    }
}

const val KEY_HAS_PREMIUM = "has_premium"
const val KEY_TRIAL_YEAR_DAYS = "trial_year_days"
const val DEFAULT_TRIAL_YEAR_DAYS = 3
const val DEFAULT_TRIAL_MONTH_DAYS = 0
const val DEFAULT_ADAPTY_ACCESS = "premium"