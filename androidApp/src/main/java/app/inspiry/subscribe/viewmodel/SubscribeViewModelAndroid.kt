package app.inspiry.subscribe.viewmodel

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.log.KLogger
import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.manager.DEFAULT_ADAPTY_ACCESS
import app.inspiry.core.manager.InspRemoteConfig
import app.inspiry.core.manager.LicenseManager
import app.inspiry.helpers.logError
import app.inspiry.subscribe.model.DisplayProduct
import app.inspiry.subscribe.model.DisplayProductPeriod
import app.inspiry.utils.Constants
import com.adapty.Adapty
import com.adapty.errors.AdaptyError
import com.adapty.models.GoogleValidationResult
import com.adapty.models.PeriodUnit
import com.adapty.models.ProductModel
import com.adapty.models.PurchaserInfoModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SubscribeViewModelAndroid(
    remoteConfig: InspRemoteConfig,
    loggerGetter: LoggerGetter,
    analyticsManager: AnalyticsManager,
    licenseManager: LicenseManager,
) : BaseSubscribeViewModel(remoteConfig, analyticsManager, licenseManager) {


    private val _uiVersionFlow: MutableStateFlow<String?> = MutableStateFlow(null)
    val uiVersionFlow: StateFlow<String?> = _uiVersionFlow

    val logger: KLogger = loggerGetter.getLogger("SubscribeViewModelAndroid")

    init {
        viewModelScope.launch {
            val uiVersion =
                remoteConfig.getValueWhenActivatedWithTimeout("subscribe_screen_ui_version")
            _uiVersionFlow.emit(uiVersion)
        }
    }

    fun onCreate(bundle: Bundle?, intent: Intent) {

        initSource(intent.getStringExtra(Constants.EXTRA_SOURCE) ?: "")
        handleIntent(intent)
    }

    fun handleIntent(intent: Intent) {

        if (Adapty.handlePromoIntent(intent) { promo, error ->

                var promoProducts: List<ProductModel>? = null

                logger.debug {
                    "handle adapty products ${promo?.paywall?.products}, " +
                            "promoId ${promo?.variationId}, paywall ${promo?.paywall?.name}, " +
                            "errorMessage = ${error?.message}, errorCode ${error?.adaptyErrorCode}, originalError ${error?.originalError}"
                }

                source = "promo ${promo?.variationId}"
                error.logError()

                if (promo?.paywall?.products != null) {
                    Adapty.logShowPaywall(promo.paywall!!)
                    promoProducts = promo.paywall?.products!!
                }

                if (promoProducts == null) {

                    loadDefaultProductsIfAbsent()
                } else {
                    onProductsLoadedLocal(promoProducts)
                }
            }
        ) {
            // your logic for the case user did click on promo notification,
            // for example show loading indicator

        } else {
            // your logic for other cases

            loadDefaultProductsIfAbsent()
        }
    }

    private fun onProductsLoadedLocal(list: List<ProductModel>) {
        onProductsLoaded(list.convertToDisplayProduct())
    }

    override fun loadProducts(paywallId: String) {
        Adapty.getPaywalls { paywalls, _, error ->
            if (error == null && paywalls != null) {
                val paywall =
                    paywalls.firstOrNull { it.developerId == paywallId } ?: paywalls.first()
                Adapty.logShowPaywall(paywall)
                onProductsLoadedLocal(paywall.products)
            } else if (error != null) {
                onError(error)
            } else if (paywalls == null) {
                onError(IllegalStateException("paywalls are null"))
            }
        }
    }
}

fun createOnClickSubscribeHandler(activity: Activity): OnClickSubscribeHandler {

    val handler = object: OnClickSubscribeHandler {

        override fun makePurchase(
            product: DisplayProduct,
            onResult: (isActivated: Boolean, vendorProductId: String?) -> Unit
        ) {

            Adapty.makePurchase(
                activity,
                product.underlyingModel as ProductModel

            ) { purchaserInfoModel: PurchaserInfoModel?, s: String?,
                googleValidationResult: GoogleValidationResult?, productModel: ProductModel,
                adaptyError: AdaptyError? ->

                adaptyError.logError()

                val isActivated = purchaserInfoModel?.accessLevels?.get(DEFAULT_ADAPTY_ACCESS)?.isActive == true
                val vendorId = productModel.vendorProductId

                onResult(isActivated, vendorId)
            }
        }
    }
    return handler
}

private fun PeriodUnit?.convertPeriod(): DisplayProductPeriod {
    if (this == null) return DisplayProductPeriod.LIFETIME
    return when (this) {
        PeriodUnit.Y -> DisplayProductPeriod.YEAR
        PeriodUnit.M -> DisplayProductPeriod.MONTH
        PeriodUnit.W -> DisplayProductPeriod.WEEK
        PeriodUnit.D -> DisplayProductPeriod.DAY
    }
}

private fun List<ProductModel>.convertToDisplayProduct(): List<DisplayProduct> {
    return map {
        DisplayProduct(
            it.localizedPrice ?: "", it.price,
            it.freeTrialPeriod?.numberOfUnits ?: 0,
            it.vendorProductId, it.subscriptionPeriod?.unit.convertPeriod(), it
        )
    }
}

class SubscribeViewModelAndroidFactory(
    private val remoteConfig: InspRemoteConfig,
    private val loggerGetter: LoggerGetter,
    private val analyticsManager: AnalyticsManager,
    private val licenseManager: LicenseManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SubscribeViewModelAndroid(
            remoteConfig,
            loggerGetter,
            analyticsManager,
            licenseManager
        ) as T
    }
}