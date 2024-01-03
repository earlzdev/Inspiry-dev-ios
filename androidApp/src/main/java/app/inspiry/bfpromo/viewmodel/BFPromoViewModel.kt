package app.inspiry.bfpromo.viewmodel

import android.app.Activity
import app.inspiry.bfpromo.BFPromoData
import app.inspiry.core.data.InspResponse
import kotlinx.coroutines.flow.StateFlow

interface BFPromoViewModel {
    val state: StateFlow<InspResponse<BFPromoData>?>
    fun onClickSubscribe(activity: Activity)
    fun reload()
}