package app.inspiry.bfpromo.viewmodel

import android.app.Activity
import app.inspiry.BuildConfig
import app.inspiry.bfpromo.BFPromoData
import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.data.InspResponse
import app.inspiry.core.data.InspResponseData
import app.inspiry.core.data.InspResponseError
import app.inspiry.core.manager.DEFAULT_ADAPTY_ACCESS
import app.inspiry.core.manager.LicenseManager
import app.inspiry.helpers.logError
import app.inspiry.utils.printDebug
import com.adapty.Adapty
import com.adapty.errors.AdaptyError
import com.adapty.models.GoogleValidationResult
import com.adapty.models.PaywallModel
import com.adapty.models.ProductModel
import com.adapty.models.PurchaserInfoModel
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONException
import org.json.JSONObject
import kotlin.coroutines.suspendCoroutine
import kotlin.math.roundToInt

class BFPromoViewModelImpl(
    private val source: String,
    private val analyticsManager: AnalyticsManager,
    private val licenseManager: LicenseManager
) : ViewModel(), BFPromoViewModel {

    private var product: ProductModel? = null
    private val _state = MutableStateFlow<InspResponse<BFPromoData>?>(null)
    override val state: StateFlow<InspResponse<BFPromoData>?>
        get() = _state

    init {
        viewModelScope.launch {
            loadProductAndConvertToState()
        }
    }

    override fun reload() {
        viewModelScope.launch {
            _state.emit(null)
            loadProductAndConvertToState()
        }
    }


    private suspend fun emitLoadedProduct(data: NecessaryProductData) {
        Adapty.logShowPaywall(data.paywall)
        this@BFPromoViewModelImpl.product = data.productDiscounted

        val discountPercent = if (GET_PERCENT_DISCOUNT_FROM_JSON) {
            data.paywall.customPayloadString?.let {
                try {
                    JSONObject(it).getInt("discount_percent")
                } catch (e: JSONException) {
                    data.calculateDiscountPercent()
                }
            } ?: data.calculateDiscountPercent()
        } else data.calculateDiscountPercent()

        val displayProduct = BFPromoData(
            discountPercent,
            data.productOld.localizedPrice ?: "", data.productDiscounted.localizedPrice ?: ""
        )

        _state.emit(InspResponseData(displayProduct))
    }

    private suspend fun emitException(e: Exception) {
        this.product = null
        e.printDebug()
        if (RANDOM_DATA_ON_ERROR) {
            _state.emit(InspResponseData(BFPromoData(40, "29.95$", "11.45%")))
        } else {
            _state.emit(InspResponseError(e))
        }
    }

    private suspend fun loadProductAndConvertToState() {
        try {
            val data = withTimeout(20000L) {
                loadNecessaryProduct()
            }
            emitLoadedProduct(data)
        } catch (e: Exception) {
            emitException(e)
        }
    }

    private suspend fun loadNecessaryProduct(): NecessaryProductData =
        suspendCoroutine {

            Adapty.getPaywalls { paywalls, products, error ->
                if (error == null && paywalls != null) {
                    val paywall =
                        paywalls.firstOrNull { it.developerId == ADAPTY_BF_PAYWALL }

                    if (paywall == null || paywall.products.size != 2) {
                        it.resumeWith(
                            Result.failure(
                                IllegalStateException(
                                    "couldn't find necessary " +
                                            "paywall ${paywalls.map { it.developerId }}. Products ${paywall?.products?.size}"
                                )
                            )
                        )
                    } else {
                        it.resumeWith(
                            Result.success(
                                NecessaryProductData(
                                    paywall,
                                    paywall.products[0],
                                    paywall.products[1]
                                )
                            )
                        )
                    }

                } else if (error != null) {
                    it.resumeWith(Result.failure(error))
                } else {
                    it.resumeWith(Result.failure(IllegalStateException("Paywalls are null")))
                }
            }
        }


    override fun onClickSubscribe(activity: Activity) {
        val product = product
        if (product != null) {
            Adapty.makePurchase(
                activity,
                product
            ) { purchaserInfoModel: PurchaserInfoModel?, s: String?,
                googleValidationResult: GoogleValidationResult?, productModel: ProductModel,
                adaptyError: AdaptyError? ->

                adaptyError.logError()

                if (purchaserInfoModel?.accessLevels?.get(DEFAULT_ADAPTY_ACCESS)?.isActive == true) {
                    licenseManager.onPurchaseActivated(product.vendorProductId)
                }
            }

            analyticsManager.onSubscribeClick(source, ANALYTICS_ID_BUTTON_CLICK)
        }
    }
}

data class NecessaryProductData(
    val paywall: PaywallModel,
    val productOld: ProductModel,
    val productDiscounted: ProductModel
) {
    fun calculateDiscountPercent(): Int {

        // we don't need that much precision. BigDecimal produces some error message thou works the same
        return ((productOld.price.toDouble() - productDiscounted.price.toDouble()) * 100 / productOld.price.toDouble()).roundToInt()
        //return productOld.price.minus(productDiscounted.price).multiply(BigDecimal(100))
        //    .divide(productOld.price).toInt()
    }
}

private const val GET_PERCENT_DISCOUNT_FROM_JSON = false
private val RANDOM_DATA_ON_ERROR = BuildConfig.DEBUG
private const val ANALYTICS_ID_BUTTON_CLICK = "subscription_year_bf"
private const val ADAPTY_BF_PAYWALL = "default-bf-1"