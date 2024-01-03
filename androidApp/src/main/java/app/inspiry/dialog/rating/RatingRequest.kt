package app.inspiry.dialog.rating

import android.content.Context
import androidx.fragment.app.FragmentActivity
import app.inspiry.core.manager.InspRemoteConfig

class RatingRequest(val context: Context, val remoteConfig: InspRemoteConfig) {

    /**
     * @param afterSharing - show dialog after share video or image
     * @param alwaysNewDialog - new_rate_us_dialog will be ignored, if value is true
     */
    fun showRatingDialog(afterSharing: Boolean = false, alwaysNewDialog: Boolean = false) {

        val isNewDialog = remoteConfig.getBoolean("new_rate_us_dialog") || alwaysNewDialog
        val afterShareEnabled = remoteConfig.getBoolean("rate_us_dialog_after_share")

        if (afterSharing != afterShareEnabled) return

        val helper = RatingDialogHelper(if (isNewDialog) RateUsDialog.DIALOG_ID else RatingDialog.DIALOG_ID)

        if (helper.needToShow(oldDialog = !isNewDialog)) {
            if (isNewDialog) showNewDialog(afterSharing)
            else showOldDialog()
        }
    }

    private fun showOldDialog() = RatingDialog.Builder(context)
        .threshold(5f)
        .build()
        .show()

    private fun showNewDialog(afterSharing: Boolean) {
        val text = if (afterSharing) context.getString(app.inspiry.projectutils.R.string.rating_dialog_share_text)
        else context.getString(app.inspiry.projectutils.R.string.rating_dialog_start_text)
        RateUsDialog.create(text)
            .show((context as FragmentActivity).supportFragmentManager, "TAG_RATE_US_DIALOG")
    }
}