package app.inspiry.helpers

import app.inspiry.utils.printDebug
import com.adapty.errors.AdaptyError


fun AdaptyError?.logError() {
    if (this != null) {
        this.originalError?.printDebug()
        K.d("adapty error") {
            "${this.message}, code ${this.adaptyErrorCode}"
        }
    }
}
