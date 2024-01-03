package app.inspiry.music.viewmodel

import app.inspiry.music.model.AlbumsResponse
import app.inspiry.music.model.CacheResponse
import app.inspiry.music.model.TracksResponse
import app.inspiry.music.provider.MusicLibraryProvider
import app.inspiry.core.log.KLogger
import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.data.InspResponse
import app.inspiry.core.data.InspResponseData
import app.inspiry.core.data.InspResponseError
import app.inspiry.core.data.InspResponseLoading
import com.soywiz.klock.DateTime
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow

class BaseMusicViewModel(
    private val initialAlbumId: Long, initLoadingOnCreate: Boolean,
    val provider: MusicLibraryProvider, loggerGetter: LoggerGetter
) :
    ViewModel() {

    val albumsState = MutableStateFlow<InspResponse<AlbumsResponse>>(InspResponseLoading())
    val tracksState = MutableStateFlow<InspResponse<TracksResponse>>(InspResponseLoading())
    val selectedAlbumIdState = MutableStateFlow(initialAlbumId)
    val searchQueryState = MutableStateFlow("")

    val logg: KLogger = loggerGetter.getLogger("BaseMusicViewModel")

    var jobLoadTracks: Job? = null


    init {
        if (initLoadingOnCreate)
            initialLoading()
    }

    fun initialLoading() {
        loadAlbums(CacheMode.BOTH, showLoadingIndicator = true)
        loadTracks(
            CacheMode.BOTH,
            initialAlbumId,
            showLoadingIndicator = true
        )
    }

    fun loadAlbums(
        cacheMode: CacheMode, notifyCacheError: Boolean = false,
        showLoadingIndicator: Boolean = false
    ) {
        loadAlbumsInner(
            cacheMode, albumsState, notifyCacheError,
            showLoadingIndicator, viewModelScope, logg, provider
        )
    }

    fun loadTrackOnClickAlbum(albumId: Long) {
        loadTracks(
            CacheMode.BOTH, albumId,
            notifyCacheError = false, showLoadingIndicator = true
        )
    }

    fun retryTracksOnError() {
        loadTracks(CacheMode.ONLY_REMOTE, selectedAlbumIdState.value, showLoadingIndicator = true)
    }

    fun retryAlbumsOnError() {
        loadAlbums(CacheMode.ONLY_REMOTE, showLoadingIndicator = true)
    }

    //-1 means load first album
    fun loadTracks(
        cacheMode: CacheMode, albumId: Long = -1L, notifyCacheError: Boolean = false,
        showLoadingIndicator: Boolean = false
    ) {
        if (jobLoadTracks?.isActive == true) {
            jobLoadTracks?.cancel()
            jobLoadTracks = null
        }

        selectedAlbumIdState.value = albumId

        jobLoadTracks = loadTracksInner(
            albumId, selectedAlbumIdState, cacheMode,
            tracksState, notifyCacheError, showLoadingIndicator, viewModelScope, logg, provider
        )
    }

    enum class CacheMode {
        ONLY_CACHE, ONLY_REMOTE, BOTH
    }

    companion object {
        const val CACHE_ALIVE_TIME = 1000L * 60 * 30
    }
}

private fun <T : MusicLibraryProvider> loadAlbumsInner(
    cacheMode: BaseMusicViewModel.CacheMode,
    state: MutableStateFlow<InspResponse<AlbumsResponse>>, notifyCacheError: Boolean,
    showLoadingIndicator: Boolean, scope: CoroutineScope, logg: KLogger, provider: T
): Job {
    return load(
        cacheMode,
        state,
        notifyCacheError, showLoadingIndicator, scope, logg, provider.supportsCache,
        provider::getAlbums,
        provider::getAlbumsCache
    )
}

private fun <T : MusicLibraryProvider> loadTracksInner(
    albumId: Long,
    selectedAlbumIdState: MutableStateFlow<Long>,
    cacheMode: BaseMusicViewModel.CacheMode,
    state: MutableStateFlow<InspResponse<TracksResponse>>,
    notifyCacheError: Boolean,
    showLoadingIndicator: Boolean,
    scope: CoroutineScope,
    logg: KLogger,
    provider: T
): Job {

    suspend fun getTracksOverrideAlbumId(getTracks: suspend (Long) -> TracksResponse): TracksResponse {
        val tracksResponse = getTracks(albumId)

        if (selectedAlbumIdState.value == -1L)
            selectedAlbumIdState.emit(tracksResponse.album.id)

        return tracksResponse

    }

    suspend fun getCacheTracksOverrideAlbumId(getTracks: suspend (Long) -> CacheResponse<TracksResponse>): CacheResponse<TracksResponse> {
        val tracksResponse = getTracks(albumId)

        if (selectedAlbumIdState.value == -1L)
            selectedAlbumIdState.emit(tracksResponse.data.album.id)

        return tracksResponse

    }

    return load(cacheMode,
        state,
        notifyCacheError, showLoadingIndicator, scope, logg, provider.supportsCache,
        {
            getTracksOverrideAlbumId(provider::getTracks)
        },
        { getCacheTracksOverrideAlbumId(provider::getTracksCache) })
}

private fun <T> load(
    cacheMode: BaseMusicViewModel.CacheMode,
    state: MutableStateFlow<InspResponse<T>>,
    notifyCacheError: Boolean,
    showLoadingIndicator: Boolean,
    scope: CoroutineScope,
    logg: KLogger,
    supportsCache: Boolean,
    getRemote: suspend () -> T,
    getCache: suspend () -> CacheResponse<T>
): Job {

    return scope.launch(Dispatchers.Default) {

        if (showLoadingIndicator)
            state.value = InspResponseLoading()


        suspend fun remote(onError: (Throwable) -> Unit) {
            try {
                val data = getRemote()
                state.value = InspResponseData(data)
            } catch (ignored: CancellationException) {
            } catch (e: Throwable) {
                e.printStackTrace()
                onError(e)
            }
        }

        // return last modif cache time
        suspend fun cache(): Long {
            try {
                val data = getCache()
                state.value = InspResponseData(data.data)
                return data.lastModifCacheTime
            } catch (ignored: CancellationException) {
            } catch (e: Throwable) {
                if (notifyCacheError) state.value = InspResponseError(e)
            }
            return 0L
        }

        if (cacheMode == BaseMusicViewModel.CacheMode.ONLY_CACHE && supportsCache)
            cache()
        else if (cacheMode == BaseMusicViewModel.CacheMode.ONLY_REMOTE)
            remote {
                state.value = InspResponseError(it)
            }
        else {
            if (supportsCache) {
                val lastModifCacheTime = cache()

                // query remote only if cache is not alive yet
                val cacheExpired =
                    (DateTime.now().unixMillisLong - lastModifCacheTime) > BaseMusicViewModel.CACHE_ALIVE_TIME

                logg.debug {
                    "CacheMode.BOTH expired ${cacheExpired}, cacheIntervalMin" +
                            " ${(DateTime.now().unixMillisLong - lastModifCacheTime) / 60000L}"
                }
                if (cacheExpired) {
                    remote {
                        if (lastModifCacheTime == 0L) {
                            state.value = InspResponseError(it)
                        }
                    }
                }

            } else {
                remote {
                    state.value = InspResponseError(it)
                }
            }
        }
    }
}
