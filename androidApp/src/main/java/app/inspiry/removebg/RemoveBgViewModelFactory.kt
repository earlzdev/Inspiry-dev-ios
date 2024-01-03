package app.inspiry.removebg

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.database.ExternalResourceDao
import com.russhwolf.settings.Settings
import okio.FileSystem

class RemovingBgViewModelFactory(
    private val imagePath: List<String>,
    private val processor: RemoveBgProcessor,
    private val externalResourceDao: ExternalResourceDao,
    private val analyticsManager: AnalyticsManager,
    private val source: String,
    private val settings: Settings,
    private val fileSystem: FileSystem
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RemovingBgViewModel(imagePath, processor, externalResourceDao, analyticsManager, source, settings, fileSystem) as T
    }
}