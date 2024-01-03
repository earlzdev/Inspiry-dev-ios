package app.inspiry.removebg

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.data.ResultWrapper
import app.inspiry.core.data.Size
import app.inspiry.core.database.ExternalResourceDao
import app.inspiry.core.util.FileUtils
import app.inspiry.core.util.PickMediaResult
import app.inspiry.core.util.createDefaultScope
import app.inspiry.core.util.withScheme
import app.inspiry.edit.instruments.PickedMediaType
import com.russhwolf.settings.Settings
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.FileSystem

class RemovingBgViewModel(
    private val imagePaths: List<String>,
    private val processor: RemoveBgProcessor,
    private val externalResourceDao: ExternalResourceDao,
    private val analyticsManager: AnalyticsManager,
    private val source: String,
    private val settings: Settings,
    private val fileSystem: FileSystem,
    private val onReceived: ((List<PickMediaResult>?) -> Unit)? = null
) : ViewModel() {

    private val _processedImage = MutableStateFlow<ResultWrapper<List<PickMediaResult>>?>(null)
    val processedImage: StateFlow<ResultWrapper<List<PickMediaResult>>?> = _processedImage

    init {
        load()
    }

    // Pair<NewlyProcessed else cached, PickMediaResultItem>
    private suspend fun loadSingle(
        index: Int,
        originalPhoto: String
    ): Pair<Boolean, PickMediaResult> {

        var newFile: String? =
            externalResourceDao.getExistingResourceAndIncrementCount(existingName = originalPhoto)
        val size: Size
        val newlyProcessed: Boolean
        if (newFile == null) {
            newlyProcessed = true

            newFile = RemoveBgFileManager.generateRemovedBgFile(fileSystem, index)
            size = processor.removeBg(originalPhoto, newFile)

            externalResourceDao.onGetNewResource(originalPhoto, newFile)
        } else {
            newlyProcessed = false
            size = processor.getSizeOfExistingFile(newFile)
        }

        return newlyProcessed to PickMediaResult(
            newFile.withScheme(FileUtils.FILE_SCHEME),
            PickedMediaType.MEDIA,
            size
        )
    }

    private fun load() {
        viewModelScope.launch {

            try {
                createDefaultScope().launch {
                    val result = imagePaths.mapIndexed { index, s ->
                        val def = async {
                            loadSingle(index, s)
                        }
                        def.await()
                    }

                    val numOfProcessedImages: Int = result.sumOf { if (it.first) 1.toInt() else 0 }

                    if (numOfProcessedImages >= 1) {
                        analyticsManager.sendEvent("image_remove_bg", createParams = {
                            put("source", source)
                            put("count", numOfProcessedImages)
                        })

                        val accProcessedImages = settings.getInt(KEY_NUM_PROCESSED_IMAGES)
                        settings.putInt(
                            KEY_NUM_PROCESSED_IMAGES,
                            accProcessedImages + numOfProcessedImages
                        )
                    }

                    _processedImage.emit(ResultWrapper(Result.success(result.map { it.second })))
                    onReceived?.invoke(result.map { it.second })
                }
            } catch (e: Exception) {
                _processedImage.emit(ResultWrapper(Result.failure(e)))
            }
        }
    }

    companion object {
        const val SOURCE_INSTRUMENT = "instrument"
        const val SOURCE_TEMPLATE = "template"
        const val KEY_NUM_PROCESSED_IMAGES = "key_num_processed_images"
    }
}