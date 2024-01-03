package app.inspiry.bfpromo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.log.ErrorHandler
import app.inspiry.core.manager.LicenseManager

class BFPromoViewModelImplFactory(
    private val source: String,
    private val analyticsManager: AnalyticsManager,
    private val licenseManager: LicenseManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BFPromoViewModelImpl(source, analyticsManager, licenseManager) as T
    }
}