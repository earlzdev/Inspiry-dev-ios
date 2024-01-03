package app.inspiry.export.viewmodel

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ResolveInfo
import android.net.Uri
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import app.inspiry.activities.MainActivity
import app.inspiry.export.ExportState
import app.inspiry.export.WhereToExport
import app.inspiry.export.whereToExportGallery
import app.inspiry.export.whereToExportMore
import app.inspiry.utils.printDebug
import app.inspiry.utils.shareMediaToApp
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel with methods that require activity, so we don't extend it from ViewModel to prevent memory leaks
 */
class ExportViewModelImpl(val activity: FragmentActivity, val viewModel: RecordViewModel) :
    ExportViewModel {

    init {
        if (viewModel.state.value !is ExportState.Rendered)
            showExportOnceWhenRendered()

    }

    private fun exportToApp(
        file: String,
        imageElseVideo: Boolean,
        whereToExport: WhereToExport,
        fromDialog: Boolean
    ) {
        if (whereToExport == whereToExportGallery) {
            // nothing. We always save it to storage
        } else if (whereToExport == whereToExportMore) {
            throw IllegalStateException()
        } else {
            activity.shareMediaToApp(
                File(file),
                viewModel.getMimeType(imageElseVideo),
                whereToExport.whereApp
            )
        }

        viewModel.sendAnalyticsShared(whereToExport, fromDialog)
    }

    override fun onClickExportInDialog(it: ResolveInfo) {
        onClickExportToApp(
            it.whereToExport, viewModel.state.value, fromDialog = true
        )
    }

    override fun onClickExportToApp(
        whereToExport: WhereToExport,
        state: ExportState,
        fromDialog: Boolean
    ) {

        if (state !is ExportState.Initial && state !is ExportState.Rendered) {
            throw IllegalStateException("state can be only initial or rendered ${state}")
        }

        if (whereToExport == whereToExportMore) {
            // nothing
            //onClickExportMore(state.imageElseVideo)

        } else if (state is ExportState.Rendered) {
            exportToApp(state.file, state.imageElseVideo, whereToExport, fromDialog)
        } else {
            viewModel.onChoiceMade(whereToExport, fromDialog, state.imageElseVideo)
        }
    }

    private var jobCollectStateUntilRendered: Job? = null
    private fun showExportOnceWhenRendered() {
        jobCollectStateUntilRendered = activity.lifecycleScope.launch {

            viewModel.state.drop(1).collect {
                if (it is ExportState.Rendered) {
                    jobCollectStateUntilRendered?.cancel()
                    jobCollectStateUntilRendered = null
                    if (it.whereToExport != null)
                        exportToApp(it.file, it.imageElseVideo, it.whereToExport!!, it.fromDialog)
                }
            }
        }
    }

    private fun mayOpenInGallery(activity: Activity, finalUri: Uri, mimeType: String) {
        val myIntent =
            Intent(Intent.ACTION_VIEW).addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        myIntent.setDataAndType(finalUri, mimeType)

        try {
            activity.startActivity(myIntent)
        } catch (ignored: ActivityNotFoundException) {
            ignored.printDebug()
        }
    }

    override fun onClickExported(imageElseVideo: Boolean) {
        val savedToGalleryUri = viewModel.stateSaveToGalleryUri.value
        if (savedToGalleryUri != null && OPEN_FILE_AFTER_SAVE_TO_GALLERY)
            mayOpenInGallery(
                activity,
                savedToGalleryUri,
                viewModel.getMimeType(imageElseVideo)
            )

        showLabelWhereSaved()
    }

    override fun backToStartActivity() {
        activity.startActivity(
            Intent(
                activity,
                MainActivity::class.java
            ).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
    }

    private fun showLabelWhereSaved() {

        Toast.makeText(
            activity,
            activity.getString(app.inspiry.projectutils.R.string.template_is_saved_message_without_copy),
            Toast.LENGTH_LONG
        ).show()
    }
}

private const val OPEN_FILE_AFTER_SAVE_TO_GALLERY = true

val ResolveInfo.whereToExport: WhereToExport
    get() = WhereToExport(
        activityInfo.packageName,
        activityInfo.name
    )