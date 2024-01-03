package app.inspiry.bfpromo

import app.inspiry.core.manager.InspRemoteConfig
import com.russhwolf.settings.Settings
import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import com.soywiz.klock.hours

class BFPromoManager(val settings: Settings, val remoteConfig: InspRemoteConfig) {

    // localize with platform code. If null - don't show banner
    fun getBannerDisplayDate(): Long? {
        val showBannerUntilDate = remoteConfig.getLong("bf_show_banner_until_date")
        if (showBannerUntilDate == 0L) return null

        val date = DateTime(unix = showBannerUntilDate)

        if (date <= DateTime.now())
            return null
        else
            return showBannerUntilDate
    }

    fun shouldOpenBFPromoOnStart(): Boolean {

        if (getBannerDisplayDate() == null) return false

        // open promo once in this period
        val openAfterDays = remoteConfig.getDouble("open_bf_promo_on_start_after_days")

        if (openAfterDays == -1.0) {
            return true
        } else if (openAfterDays == 0.0) {
            return false
        } else {

            val lastTimeOpened = settings.getLongOrNull(keyLastTimeOpened)
            val curTimeLong = DateTime.nowUnixLong()
            if (lastTimeOpened == null) {
                // don't open on first request
                settings.putLong(keyLastTimeOpened, curTimeLong)
                return false
            } else {
                val timePassed = curTimeLong - lastTimeOpened
                val shouldOpen = timePassed > openAfterDays.days.millisecondsLong

                if (shouldOpen)
                    // save current time minus 1 hour
                    settings.putLong(keyLastTimeOpened, curTimeLong - 1.0.hours.millisecondsLong)

                return shouldOpen
            }
        }
    }
}
private const val keyLastTimeOpened = "last_time_opened_bf_promo_on_startup"