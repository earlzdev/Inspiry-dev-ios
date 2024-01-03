package app.inspiry.export.viewmodel

import android.net.Uri
import android.os.Bundle
import app.inspiry.core.data.OriginalTemplateData
import app.inspiry.core.media.Template
import app.inspiry.export.ExportState
import app.inspiry.export.WhereToExport
import kotlinx.coroutines.flow.StateFlow

interface RecordViewModel {
    val state: StateFlow<ExportState>
    val stateSaveToGalleryUri: StateFlow<Uri?>
    fun stopRecordThread()
    fun sendAnalyticsShared(whereToExport: WhereToExport, fromDialog: Boolean)
    fun onSaveInstanceState(outState: Bundle)
    fun onChangeImageElseVideo(imageElseVideo: Boolean)
    fun onChoiceMade(whereToExport: WhereToExport, fromDialog: Boolean, imageElseVideo: Boolean)
    fun onDestroy()
    fun getMimeType(imageElseVideo: Boolean): String
}