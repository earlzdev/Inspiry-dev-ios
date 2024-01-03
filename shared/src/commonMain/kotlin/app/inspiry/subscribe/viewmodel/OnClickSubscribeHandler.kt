package app.inspiry.subscribe.viewmodel

import app.inspiry.subscribe.model.DisplayProduct

interface OnClickSubscribeHandler {
    fun makePurchase(product: DisplayProduct, onResult: (isActivated: Boolean, vendorProductId: String?) -> Unit)
}