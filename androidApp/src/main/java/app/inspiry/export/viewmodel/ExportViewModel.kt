package app.inspiry.export.viewmodel

import android.content.pm.ResolveInfo
import app.inspiry.export.ExportState
import app.inspiry.export.WhereToExport

interface ExportViewModel {

    fun onClickExportToApp(
        whereToExport: WhereToExport,
        state: ExportState,
        fromDialog: Boolean
    )

    fun onClickExported(imageElseVideo: Boolean)
    fun backToStartActivity()
    fun onClickExportInDialog(it: ResolveInfo)
}