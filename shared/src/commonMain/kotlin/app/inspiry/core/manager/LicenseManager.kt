package app.inspiry.core.manager

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface LicenseManager {
    val hasPremiumState: StateFlow<Boolean>
    val displayTrialDays: MutableStateFlow<Int>
    fun restorePurchases(forceUpdate: Boolean = false)
    fun updateTrialDays()
    fun debugActivatePurchase()
    fun mayShowFreeDialogCausePaymentsFailed(): Boolean
    fun onPurchaseActivated(id: String)
}