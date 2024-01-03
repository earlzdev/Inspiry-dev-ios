package app.inspiry.export.dialog

import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.inspiry.core.database.InspDatabase

class ExportDialogViewModelFactory(
    private val inspDatabase: InspDatabase,
    private val packageManager: PackageManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ExportDialogViewModel(
            inspDatabase, packageManager
        ) as T
    }
}