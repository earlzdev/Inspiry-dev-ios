package app.inspiry.dialog.rating

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import app.inspiry.R
import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.manager.InspRemoteConfig
import app.inspiry.core.analytics.putInt
import app.inspiry.core.analytics.putString
import app.inspiry.utils.sendEmail
import com.russhwolf.settings.Settings
import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject


class RatingDialogHelper(private val dialogID: String) : KoinComponent {

    private val remoteConfig: InspRemoteConfig by inject()
    private val settings: Settings by inject()
    private val analyticsManager: AnalyticsManager by inject()

    private fun openPlayStore(context: Context) {
        val marketUri = Uri.parse("market://details?id=" + context.packageName)
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, marketUri))
            toast(context.getString(app.inspiry.projectutils.R.string.rating_dialog_please_rate), context)

        } catch (ex: ActivityNotFoundException) {
            toast("Couldn't find PlayStore on this device", context)
        }
    }

    fun sendFeedback(rating: Float, feedback: String?, context: Context) {
        feedback?.let {
            if (feedback.isNotEmpty()) (context as Activity).sendEmail(
                remoteConfig.getString("support_email"),
                "Inspiry Rating $rating", feedback
            )
        }

    }

    fun setShowNewer() {
        settings.putBoolean(KEY_RATING_DIALOG_SHOW_NEWER, true)
    }

    private fun saveNextDialogTime() {
        val nextDate = (DateTime.now() + 14.days).unixMillisLong
        settings.putLong(KEY_RATING_DIALOG_NEXT_TIME, nextDate)
    }

    fun sendLessStarsEvent(stars: Int) {
        analyticsManager.sendEvent(eventName = "rate_us_less_than_5_stars", createParams = {
            putInt("star_numbers", stars)
            putString(key = "dialogID", value = dialogID)
        })
    }

    private fun sendFiveStarsEvent() {
        analyticsManager.sendEvent(eventName = "rate_us_5_stars", createParams = {
            putString(key = "dialogID", value = dialogID)

        })

    }

    fun sendDialogOpenEvent() {
        analyticsManager.sendEvent(eventName = "rate_us_dialog_open", createParams = {
            putString(key = "dialogID", value = dialogID)

        })
    }

    fun needToShow(oldDialog: Boolean = false, force: Boolean = false): Boolean {
        // if (BuildConfig.DEBUG) return true //dialog will always be displayed in debug mode
        if (settings.getBoolean(KEY_RATING_DIALOG_SHOW_NEWER, false)) return false
        if (!checkIfSessionMatches() && !force) return false
        if (!oldDialog) {
            val nextDate = settings.getLongOrNull(KEY_RATING_DIALOG_NEXT_TIME)
            if (nextDate == null) {
                saveNextDialogTime()
                return true
            }
            if (nextDate >= DateTime.now().unixMillisLong) return false
        }
        setShowNewer()
        return true
    }

    fun rating(stars: Int, feedback: String?, context: Context) {
        when (stars) {
            5 -> onFiveStar(context = context)
            in 1..4 -> {
                setShowNewer()
                sendLessStarsEvent(stars = stars)
                feedback?.let {
                    sendFeedback(rating = stars + 0f, feedback = it, context = context)
                }
            }
        }
    }

    private fun onFiveStar(context: Context) {
        setShowNewer()
        sendFiveStarsEvent()
        openPlayStore(context)

    }

    private fun checkIfSessionMatches(): Boolean {
        val session = RATING_SESSION_THRESHOLD
        if (session == 1) {
            return true
        }
        var count: Int = settings.getInt(KEY_RATING_DIALOG_SESSION_COUNT, 1)
        return if (session <= count) {
            settings.putInt(KEY_RATING_DIALOG_SESSION_COUNT, 1)
            true
        } else {
            count++
            settings.putInt(KEY_RATING_DIALOG_SESSION_COUNT, count)
            false
        }
    }

    private fun toast(text: String, context: Context) =
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()


    companion object {
        const val KEY_RATING_DIALOG_NEXT_TIME = "rating_dialog_next_time"
        const val KEY_RATING_DIALOG_SHOW_NEWER = "rating_dialog_show_newer"
        const val KEY_RATING_DIALOG_SESSION_COUNT = "rating_dialog_session_count"
        const val RATING_DAY_THRESHOLD = 1
        const val RATING_SESSION_THRESHOLD = 4
    }
}
