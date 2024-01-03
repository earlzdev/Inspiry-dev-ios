package app.inspiry.subscribe.viewmodel

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.data.InspResponse
import app.inspiry.core.data.InspResponseData
import app.inspiry.core.data.InspResponseError
import app.inspiry.core.data.InspResponseLoading
import app.inspiry.core.helper.KotlinFormatter
import app.inspiry.core.log.GlobalLogger
import app.inspiry.core.manager.*
import app.inspiry.subscribe.model.DisplayProduct
import app.inspiry.subscribe.model.DisplayProductPeriod
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.roundToInt

data class SubscribeUiState(
    val options: List<DisplayProduct>,
    val selectedOptionPos: Int,
    val selectedOptionTrialDays: Int,
    val yearSaveAmount: String?,
    val yearPerMonthPrice: String?
) {
    override fun toString(): String {
        return "SubscribeUiState(options=$options, selectedOptionPos=$selectedOptionPos, selectedOptionTrialDays=$selectedOptionTrialDays, yearSaveAmount=$yearSaveAmount, yearPerMonthPrice=$yearPerMonthPrice)"
    }
}

abstract class BaseSubscribeViewModel(
    val remoteConfig: InspRemoteConfig,
    val analyticsManager: AnalyticsManager,
    val licenseManager: LicenseManager
) : ViewModel() {

    lateinit var source: String

    protected val _stateFlow =
        MutableStateFlow<InspResponse<SubscribeUiState>>(InspResponseLoading())
    val stateFlow: StateFlow<InspResponse<SubscribeUiState>> = _stateFlow

    val displayProductsEmpty: Boolean
        get() = stateFlow.value !is InspResponseData

    abstract fun loadProducts(paywallId: String)

    fun loadDefaultProductsIfAbsent() {
        if (displayProductsEmpty) {
            _stateFlow.value = InspResponseLoading()
            loadDefaultProducts()
        }
    }

    fun loadDefaultProducts() {
        //"default-2-no-lifetime" to test 2 buttons
        val defaultPaywall = remoteConfig.getString("default_adapty_paywall")
        loadProducts(defaultPaywall)
    }

    protected fun getTrialDaysForProductAtIndex(products: List<DisplayProduct>, index: Int): Int {
        return products.getOrNull(index)?.trialDays ?: 0
    }

    protected fun findInitialSelectedPos(products: List<DisplayProduct>): Int {
        val indexOfYear = products.indexOfFirst { it.period == DisplayProductPeriod.YEAR }
        return if (indexOfYear < 0) 0 else indexOfYear
    }

    protected fun initSource(source: String) {
        this.source = source
        analyticsManager.onSubscribeScreenOpen(source)
    }

    fun onError(e: Throwable) {
        _stateFlow.value = InspResponseError(e)
    }

    fun onSubscribeClick(handler: OnClickSubscribeHandler, onFinished:((Boolean) -> Unit)? = null) {
        if (displayProductsEmpty) {
            throw IllegalStateException("displayProducts are empty")
        }

        val data = (stateFlow.value as InspResponseData).data

        val productIndex = data.selectedOptionPos
        val product: DisplayProduct? = data.options.getOrNull(productIndex)
            //?: throw IllegalStateException("Product at index ${productIndex} is null")

        val idForAnalytics: String = when (product?.period) {
            DisplayProductPeriod.MONTH -> "subscription_month"
            DisplayProductPeriod.YEAR -> "subscription_year"
            DisplayProductPeriod.WEEK -> "subscription_week"
            null -> "display product is null!!"
            else -> "purchase_forever"
        }

        analyticsManager.onSubscribeClick(source, idForAnalytics)
        if (product != null) {
            handler.makePurchase(product) { isActivated: Boolean, vendorProductId: String? ->
                if (isActivated) {
                    licenseManager.onPurchaseActivated(vendorProductId!!)
                }
                onFinished?.invoke(isActivated)
            }
        } else {
            onFinished?.invoke(false)
        }
    }

    fun onDebugSubscribeLongClick() {
        if (DebugManager.isDebug)
            licenseManager.debugActivatePurchase()
    }

    fun onProductsLoaded(displayProducts: List<DisplayProduct>) {

        val selectedPos =
            (_stateFlow.value as? InspResponseData<SubscribeUiState>?)?.data?.selectedOptionPos
                ?: findInitialSelectedPos(displayProducts)


        val newData = SubscribeUiState(
            options = displayProducts,
            selectedOptionTrialDays = getTrialDaysForProductAtIndex(
                displayProducts,
                selectedPos
            ),
            yearSaveAmount = calcYearSaveAmount(displayProducts),
            yearPerMonthPrice = calcYearPerMonthPrice(displayProducts),
            selectedOptionPos = selectedPos
        )

        _stateFlow.value = InspResponseData(newData)
    }

    fun onOptionSelected(pos: Int) {

        val data = (_stateFlow.value as? InspResponseData<SubscribeUiState>?)?.data
            ?: throw IllegalStateException("data is null")

        _stateFlow.value = InspResponseData(
            data.copy(
                selectedOptionPos = pos,
                selectedOptionTrialDays = getTrialDaysForProductAtIndex(data.options, pos)
            )
        )
    }

    private fun calcYearSaveAmount(products: List<DisplayProduct>): String? {
        val productMonth =
            products.firstOrNull { it.period == DisplayProductPeriod.MONTH }?.price
        val productYear =
            products.firstOrNull { it.period == DisplayProductPeriod.YEAR }?.price

        val saveAmountFloat =
            if (productMonth != null && productYear != null && productMonth != 0 && productYear != 0) {
                (1.0 - (productYear.toDouble() / (productMonth.toDouble() * 12))).toFloat() * 100
            } else
                return null

        if (saveAmountFloat <= 0f) return null

        val roundedSaveAmount = roundSaveAmountToNearestMultiple5(saveAmountFloat)

        return roundedSaveAmount.toString()
    }

    /**
     * Пример:
     * 74 / 5 = 14.8
     * Округляем до 15
     * 15 * 5 = 75
     */
    private fun roundSaveAmountToNearestMultiple5(saveAmount: Float): Int {
        return 5 * ((saveAmount / 5).roundToInt())
    }

    private fun calcYearPerMonthPrice(products: List<DisplayProduct>): String? {
        val yearProduct = products.firstOrNull { it.period == DisplayProductPeriod.YEAR }

        yearProduct?.let {
            val monthPriceFloat = it.price.toFloat() / 12f
            GlobalLogger.debug("SubscribeModel") {"month price float $monthPriceFloat"}
            val currencySymbol = findCurrencySymbol(it.localizedPrice)
            GlobalLogger.debug("SubscribeModel") {"currency simbol $currencySymbol"}
            //var monthPriceString = KotlinFormatter.format("%.1f", monthPriceFloat) //todo format does not work correctly in ios
            var monthPriceString = ((monthPriceFloat * 100f).toInt() / 100f).toString()
            GlobalLogger.debug("SubscribeModel") {"monthPriceString $monthPriceString)"}
            if (it.localizedPrice.contains("."))
                monthPriceString = monthPriceString.replace(",", ".")

            return if (it.localizedPrice.startsWith(currencySymbol)) {
                resolveWhiteSpace(it.localizedPrice, currencySymbol, monthPriceString)
            } else {
                resolveWhiteSpace(it.localizedPrice, monthPriceString, currencySymbol)
            }
        } ?: return null
    }

    private fun resolveWhiteSpace(localizedPrice: String, arg1: String, arg2: String): String {
        return if (localizedPrice.contains(Regex("[\\s]")))
            "$arg1 $arg2"
        else
            "$arg1$arg2"
    }

    private fun findCurrencySymbol(price: String): String {
        return price.replace(Regex("[0-9,.\\s]"), "")
    }
}

const val SUBSCRIBE_SOURCE_ROYALTY_FREE_MUSIC = "royalty_free_music"