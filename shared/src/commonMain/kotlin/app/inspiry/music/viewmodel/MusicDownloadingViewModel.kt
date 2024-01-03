package app.inspiry.music.viewmodel

import app.inspiry.MR
import app.inspiry.core.data.*
import app.inspiry.core.database.ExternalResourceDao
import app.inspiry.core.manager.InspRemoteConfig
import app.inspiry.core.manager.LicenseManager
import app.inspiry.core.util.FileUtils
import app.inspiry.core.util.withScheme
import app.inspiry.music.client.MusicFileCreator
import app.inspiry.music.client.TrackDownloader
import app.inspiry.music.model.Album
import app.inspiry.music.model.MusicTab
import app.inspiry.music.model.TemplateMusic
import app.inspiry.music.model.Track
import com.russhwolf.settings.Settings
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import dev.icerock.moko.resources.StringResource
import io.ktor.client.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.math.max

class MusicDownloadingViewModel(
    private val musicFileCreator: MusicFileCreator,
    httpClient: HttpClient,
    private val fileSystem: FileSystem,
    private val externalResourceDao: ExternalResourceDao,
    private val remoteConfig: InspRemoteConfig,
    private val settings: Settings,
    private val licenseManager: LicenseManager
) : ViewModel() {

    private val _downloadingState =
        MutableStateFlow<InspResponse<TemplateMusic>>(InspResponseNothing())
    val downloadingState: StateFlow<InspResponse<TemplateMusic>> = _downloadingState
    private val trackDownloader = TrackDownloader(httpClient)

    private fun getRoyaltyFreeTracksLeft(): Int = max(
        remoteConfig.getInt(
            KEY_ROYALTY_FREE_MUSIC_TRIES
        ) - settings.getInt(KEY_ROYALTY_FREE_MUSIC_TRIES, 0), 0
    )

    fun onPickMusicHandled() {
        _downloadingState.value = InspResponseNothing()
    }

    fun getTextRoyaltyFreeTracksLeft(resToString: (StringResource) -> String): String {

        val tracksLeft = getRoyaltyFreeTracksLeft()

        return if (tracksLeft > 0)
            resToString(MR.strings.music_free_tracks_left) + " " + tracksLeft.toString()
        else resToString(MR.strings.banner_trial_subtitle)
    }

    fun getRoyaltyFreeTracksLeftProgress(): Float {

        val userPricked = settings.getInt(
            KEY_ROYALTY_FREE_MUSIC_TRIES, 0)

        if (userPricked == 0) return 1f
        return 1f - max(userPricked.toFloat() / remoteConfig.getInt(KEY_ROYALTY_FREE_MUSIC_TRIES), 0f)
    }

    fun shouldOpenSubscribeOnPickMusic(tab: MusicTab, hasPremium: Boolean) = tab == MusicTab.LIBRARY && !hasPremium && getRoyaltyFreeTracksLeft() == 0

    fun pickMusic(item: Track, album: Album, durationMillis: Long, tab: MusicTab) {

        fun onPicked(url: String) {

            if (tab == MusicTab.LIBRARY && !licenseManager.hasPremiumState.value) {

                settings.putInt(KEY_ROYALTY_FREE_MUSIC_TRIES, settings.getInt(
                    KEY_ROYALTY_FREE_MUSIC_TRIES, 0) + 1)
            }

            _downloadingState.value = InspResponseData(
                TemplateMusic(
                    url,
                    item.title,
                    item.artist,
                    album.name,
                    durationMillis, tab = tab, albumId = album.id
                )
            )
        }

        fun downloadMusic() {
            val albumFolderName = album.name.replace(' ','_' )
            //replacing space with underline because files with spaces in path are not unreachable in ios after saving (I don't have a idea why)
            val file =
                musicFileCreator.getDownloadFile(item.url, tab.name + "/" + albumFolderName)
            if (fileSystem.exists(file)) {

                val filePath = file.toString()
                externalResourceDao.onGetNewResource(item.url, filePath)
                onPicked(filePath.withScheme(FileUtils.FILE_SCHEME))

            } else {

                viewModelScope.launch {
                    _downloadingState.emit(InspResponseLoading(0f))

                    trackDownloader.downloadFile(item.url, file, fileSystem)
                        .flowOn(Dispatchers.Default)
                        .onCompletion {
                            if (it == null) {
                                _downloadingState.emit(InspResponseNothing())

                                val filePath = file.toString()
                                externalResourceDao.onGetNewResource(item.url, filePath)
                                onPicked(filePath.withScheme(FileUtils.FILE_SCHEME))
                            }
                        }
                        .catch {
                            _downloadingState.emit(InspResponseError(it))
                        }
                        .collect {
                            _downloadingState.emit(InspResponseLoading(it))
                        }
                }
            }
        }

        if (item.url.startsWith("http")) {

            val existingFile =
                externalResourceDao.getExistingResourceAndIncrementCount(existingName = item.url)
            if (existingFile != null) {

                if (!fileSystem.exists(existingFile.toPath())) {
                    externalResourceDao.onResourceStoppedExisting(existingFile)
                    downloadMusic()
                } else {
                    onPicked(existingFile.withScheme(FileUtils.FILE_SCHEME))
                }
            } else {
                downloadMusic()
            }

        } else {
            onPicked(item.url)
        }
    }
}

private const val KEY_ROYALTY_FREE_MUSIC_TRIES = "royalty_free_music_tries"