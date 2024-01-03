package app.inspiry.stickers

import app.inspiry.core.data.InspResponse
import app.inspiry.core.data.InspResponseData
import app.inspiry.core.data.InspResponseLoading
import app.inspiry.stickers.providers.MediaWithPath
import app.inspiry.stickers.providers.StickersProvider
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StickersViewModel(val provider: StickersProvider,
                        initialCategory: String?,
                        initialStickerIndex: Int?) : ViewModel() {

    val categories: List<String>
        get() = provider.getCategories()

    val currentCategory =
        MutableStateFlow(initialCategory ?: categories.first())

    val currentStickers =
        MutableStateFlow<InspResponse<List<MediaWithPath>>>(InspResponseLoading())

    private val _currentStickerIndex =
        MutableStateFlow(if (initialStickerIndex == null || initialStickerIndex < 0) 0 else initialStickerIndex)
    val currentStickerIndex: StateFlow<Int> = _currentStickerIndex

    init {
        load(currentCategory.value, false)
    }

    private var currentJob: Job? = null

    fun setCurrentStickerIndex(index: Int) {
        _currentStickerIndex.value = index
    }

    //getting list of stickers for ios, or null while not loaded
    fun getCurrentStickers(): List<MediaWithPath>? {
        return (currentStickers.value as? InspResponseData<List<MediaWithPath>>)?.data
    }

    fun getCurrentSticker(): MediaWithPath? {
        val stickers = currentStickers.value as? InspResponseData<List<MediaWithPath>>?
        if (stickers != null) {
            val data = stickers.data.getOrNull(currentStickerIndex.value) ?: return null
            return data
        }
        return null
    }

    fun load(category: String, resetIndex: Boolean = true) {
        currentJob?.cancel()

        currentCategory.value = category
        if (resetIndex)
            setCurrentStickerIndex(0)

        currentJob = loadStickers(viewModelScope, currentStickers) { provider.getStickers(category) }
    }
}
private fun loadStickers(scope: CoroutineScope, state: MutableStateFlow<InspResponse<List<MediaWithPath>>>,
                         load: suspend () -> List<MediaWithPath>): Job {
    return scope.launch(Dispatchers.Default) {
        state.emit(InspResponseLoading())
        state.emit(InspResponseData(load()))
    }
}