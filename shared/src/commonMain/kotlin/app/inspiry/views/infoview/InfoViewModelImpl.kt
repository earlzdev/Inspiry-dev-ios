package app.inspiry.views.infoview

import app.inspiry.core.data.InspResponse
import app.inspiry.core.data.InspResponseError
import app.inspiry.core.data.InspResponseLoading
import app.inspiry.core.data.InspResponseNothing
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InfoViewModelImpl(
    val coroutineScope: CoroutineScope
): InfoViewModel {

    private val _state = MutableStateFlow<InspResponse<Unit>>(InspResponseNothing())

    override val state: StateFlow<InspResponse<Unit>> = _state

    private var jobShowLoading: Job? = null

    override fun removeInfoView() {
        cancelPreviousJobs()
        _state.value = InspResponseNothing()
    }

    override fun showLoadingTemplate() {
        showLoading(TEMPLATE_INITIALIZE_DELAY, VALUE_TEMPLATE)
    }

    override fun showLoadingImages() {
        showLoading(LOADING_IMAGES_DELAY, VALUE_IMAGES)
    }

    override fun cancelPendingProgressBar() {
        cancelPreviousJobs()
    }

    override fun showErrorAndButtonRetry(e: Throwable?) {
        cancelPreviousJobs()
        _state.value = InspResponseError(e ?: Exception())
    }

    override fun isDisplayed(): Boolean =
        state.value is InspResponseLoading || state.value is InspResponseError


    private fun cancelPreviousJobs() {
        jobShowLoading?.cancel()
    }

    private fun showLoading(delay: Long, value: Float) {
        // otherwise we already scheduled to show it
        if (jobShowLoading == null) {
            jobShowLoading = coroutineScope.launch {
                delay(delay)
                _state.emit(InspResponseLoading(value))
                jobShowLoading = null
            }
        }
    }

    companion object {
        const val TEMPLATE_INITIALIZE_DELAY = 600L
        const val LOADING_IMAGES_DELAY = 400L
        const val VALUE_IMAGES = 1f
        const val VALUE_TEMPLATE = 0.8f
    }
}