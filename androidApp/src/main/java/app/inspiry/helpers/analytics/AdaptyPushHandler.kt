package app.inspiry.helpers.analytics

import android.content.Context
import app.inspiry.R
import com.adapty.push.AdaptyPushHandler

class MyAdaptyPushHandler(context: Context): AdaptyPushHandler(context) {
    override val clickAction: String = "click_adapty_notification"
    override val smallIconResId: Int = R.drawable.ic_notification
    override val channelId: String = "offers"
}