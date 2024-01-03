package app.inspiry.views.infoview

import app.inspiry.core.data.InspResponse
import app.inspiry.core.data.InspResponseError
import app.inspiry.core.data.InspResponseLoading
import kotlinx.coroutines.flow.StateFlow

interface InfoViewModel {
    /**
     * Can be InspResponseLoading, Error, Nothing
     */
    val state: StateFlow<InspResponse<Unit>>

    fun removeInfoView()
    fun showLoadingTemplate()
    fun showLoadingImages()
    fun cancelPendingProgressBar()
    fun showErrorAndButtonRetry(e: Throwable?)

    fun isDisplayed(): Boolean = state.value is InspResponseLoading || state.value is InspResponseError
}