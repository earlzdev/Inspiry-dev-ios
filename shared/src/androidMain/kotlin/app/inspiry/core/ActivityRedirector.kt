package app.inspiry.core

import android.app.Activity

interface ActivityRedirector {
    fun openSubscribeActivity(activity: Activity, source: String)
    fun openBFPromoActivity(activity: Activity, source: String)
}