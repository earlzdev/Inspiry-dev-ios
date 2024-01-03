package app.inspiry.music.android.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.inspiry.core.database.ExternalResourceDao
import app.inspiry.core.manager.InspRemoteConfig
import app.inspiry.core.manager.LicenseManager
import app.inspiry.music.client.MusicFileCreator
import app.inspiry.music.viewmodel.MusicDownloadingViewModel
import com.russhwolf.settings.Settings
import io.ktor.client.*
import okio.FileSystem

class MusicDownloadingViewModelFactory(
    private val musicFileCreator: MusicFileCreator,
    private val httpClient: HttpClient,
    private val fileSystem: FileSystem,
    private val externalResourceDao: ExternalResourceDao,
    private val remoteConfig: InspRemoteConfig,
    private val settings: Settings,
    private val licenseManger: LicenseManager
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return MusicDownloadingViewModel(
            musicFileCreator,
            httpClient,
            fileSystem,
            externalResourceDao,
            remoteConfig,
            settings,
            licenseManger
        ) as T
    }
}