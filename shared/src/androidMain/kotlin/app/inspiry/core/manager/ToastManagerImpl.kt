package app.inspiry.core.manager

import android.content.Context
import android.widget.Toast

class ToastManagerImpl(val context: Context) : ToastManager {

    override fun displayToast(text: String, length: ToastLength) {

        Toast.makeText(
            context, text, when (length) {
                ToastLength.SHORT -> Toast.LENGTH_SHORT
                ToastLength.LONG -> Toast.LENGTH_LONG
            }
        ).show()
    }
}