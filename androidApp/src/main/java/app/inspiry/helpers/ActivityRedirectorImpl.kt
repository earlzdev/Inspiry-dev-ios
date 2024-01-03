package app.inspiry.helpers

import android.app.Activity
import android.content.Intent
import app.inspiry.bfpromo.ui.BFPromoActivity
import app.inspiry.core.ActivityRedirector
import app.inspiry.subscribe.ui.SubscribeActivity
import app.inspiry.utils.Constants

class ActivityRedirectorImpl : ActivityRedirector {
    override fun openSubscribeActivity(activity: Activity, source: String) {
        activity.startActivity(
            Intent(activity, SubscribeActivity::class.java)
                .putExtra(Constants.EXTRA_SOURCE, source)
        )
    }

    override fun openBFPromoActivity(activity: Activity, source: String) {
        activity.startActivity(
            Intent(activity, BFPromoActivity::class.java)
                .putExtra(Constants.EXTRA_SOURCE, source)
        )
    }
}