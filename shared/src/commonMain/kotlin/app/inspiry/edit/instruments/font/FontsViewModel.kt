package app.inspiry.edit.instruments.font

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.data.InspResponse
import app.inspiry.core.data.InspResponseData
import app.inspiry.core.data.InspResponseLoading
import app.inspiry.core.data.OriginalTemplateData
import app.inspiry.core.manager.LicenseManager
import app.inspiry.core.util.removeExt
import app.inspiry.core.util.removeScheme
import app.inspiry.edit.instruments.BottomInstrumentsViewModel
import app.inspiry.font.helpers.TextCaseHelper
import app.inspiry.font.model.*
import app.inspiry.font.provider.FontsManager
import app.inspiry.font.provider.PlatformFontPathProvider
import app.inspiry.font.provider.UploadedFontsProvider
import app.inspiry.views.InspView
import app.inspiry.views.text.InspTextView
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FontsViewModel(
    inspTextView: InspTextView,
    val fontsManager: FontsManager,
    val uploadedFontsProvider: UploadedFontsProvider,
    val analyticManager: AnalyticsManager,
    val textCaseHelper: TextCaseHelper,
    val licenseManager: LicenseManager,
    val platformFontPathProvider: PlatformFontPathProvider,
    val onSubscribe: () -> Unit
) :
    ViewModel(), BottomInstrumentsViewModel {

    override fun sendAnalyticsEvent() {
        selectedView.value.templateParent.template.originalData?.let { originalData ->
            onDialogDestroy(
                initialFont,
                initialText,
                originalData
            )
        }

    }

    val selectedView = MutableStateFlow(inspTextView)

    val currentFontStyle: MutableStateFlow<InspFontStyle> =
        MutableStateFlow(inspTextView.media.font?.fontStyle ?: InspFontStyle.regular)
    val currentFontPath: MutableStateFlow<FontPath> =
        MutableStateFlow(fontsManager.getFontPathByIdWithFile(inspTextView.media.font?.fontPath))
    val currentText: MutableStateFlow<String> = MutableStateFlow(inspTextView.media.text)
    val currentCategoryIndex =
        MutableStateFlow(fontsManager.findInitialSelectedCategory(inspTextView.media.font?.fontPath))

    val currentFonts = MutableStateFlow<InspResponse<FontPathsResponse>>(InspResponseLoading())

    private var uploadCategoryCache: List<UploadedFontPath>? = null

    var onInitialFontIndexChange: ((Int) -> Unit)? = null

    val initialFont: FontData = getFontData()

    val initialText = currentText.value

    override fun onSelectedViewChanged(newSelected: InspView<*>?) {
        selectedView.value = newSelected as InspTextView
        currentFontStyle.value = selectedView.value.media.font?.fontStyle ?: InspFontStyle.regular
        currentFontPath.value = fontsManager.getFontPathByIdWithFile(selectedView.value.media.font?.fontPath)
        currentText.value = selectedView.value.media.text
        currentCategoryIndex.value = fontsManager.findInitialSelectedCategory(selectedView.value.media.font?.fontPath)
    }

    init {

        viewModelScope.launch(Dispatchers.Main) {
            uploadCategoryCache = getUploadedFontsAsync(uploadedFontsProvider)
            if (fontsManager.allCategories[currentCategoryIndex.value] == FontsManager.CATEGORY_ID_UPLOAD) {
                currentFonts.value = InspResponseData(FontPathsResponse(uploadCategoryCache!!))
            }
        }

        viewModelScope.launch {
            currentText.drop(1).collectLatest {
                selectedView.value.onCapsModeChanged(it)
            }
        }

        viewModelScope.launch {
            currentCategoryIndex.collect {

                val categoryId = fontsManager.allCategories[it]

                if (categoryId == FontsManager.CATEGORY_ID_UPLOAD) {
                    if (uploadCategoryCache == null) {
                        currentFonts.value = InspResponseLoading()

                    } else {
                        currentFonts.value =
                            InspResponseData(FontPathsResponse(uploadCategoryCache!!))
                    }
                } else {
                    currentFonts.value =
                        InspResponseData(
                            FontPathsResponse(
                                fontsManager.getFontsByCategory(
                                    categoryId
                                )
                            )
                        )
                }

            }
        }

        viewModelScope.launch {
            currentFonts.collectLatest {
                if (it is InspResponseData) {
                    onInitialFontIndexChange?.invoke(
                        fontsManager.findSelectedIndex(
                            it.data.fonts,
                            selectedView.value.media.font?.fontPath
                        )
                    )
                    cancel()
                }
            }
        }
    }

    private fun callFontChanged() {
        selectedView.value.onFontChanged(getFontData())
    }

    private fun getFontData(): FontData =
        fontsManager.getFontData(currentFontPath.value, currentFontStyle.value)

    fun onClickToggleCapsMode() {
        currentText.value = textCaseHelper.toggleCapsMode(currentText.value)
    }

    fun onPickedNewFont(path: FontPath?) {
        if (path == null) onSubscribe()
        else {
            currentFontPath.value =
                if (path is PredefinedFontPath) fontsManager.mayReplaceFontIfCyrillic(
                    currentText.value,
                    path
                )
                else path
            currentFontStyle.value = InspFontStyle.regular

            selectedView.value.onFontChanged(getFontData())
        }
    }

    fun onFontStyleChange(style: InspFontStyle) {
        if (currentFontStyle.value == style)
            currentFontStyle.value = InspFontStyle.regular
        else
            currentFontStyle.value = style

        callFontChanged()
    }

    fun onDialogDestroy(
        initialFont: FontData?,
        initialText: String,
        originalTemplateData: OriginalTemplateData
    ) {

        val currentFontPathValue = currentFontPath.value
        val currentCategory = fontsManager.allCategories[currentCategoryIndex.value]

        val newFontPath = currentFontPathValue.path
        val newFontStyle = currentFontStyle.value
        val newCapsStyle = textCaseHelper.getCurrentCapsModeForAnalytics(currentText.value)

        val isPremium = currentFontPathValue.forPremium

        analyticManager.onFontDialogClose(
            newFontPath,
            initialFont?.fontPath,
            newFontStyle.name,
            initialFont?.fontStyle?.name,
            newCapsStyle,
            textCaseHelper.getCurrentCapsModeForAnalytics(initialText),
            isPremium,
            originalTemplateData,
            currentCategory
        )
    }

    fun addUploadedFont(fontPath: UploadedFontPath) {
        if (uploadedFontsProvider.getFonts().contains(fontPath)) return //don't add the same font twice
        mayAddFontCategoryUpload(fontPath)
        onPickedNewFont(fontPath)
        analyticManager.customFontUploaded(fontPath.path.removeScheme().removeExt())
    }

    private fun mayAddFontCategoryUpload(fontPath: UploadedFontPath) {
        val oldUploadCategoryCache = this.uploadCategoryCache

        if (oldUploadCategoryCache != null) {

            uploadCategoryCache = oldUploadCategoryCache.toMutableList().also {
                it.add(fontPath)
            }

            if (oldUploadCategoryCache === (currentFonts.value as? InspResponseData)?.data?.fonts) {
                currentFonts.value = InspResponseData(FontPathsResponse(uploadCategoryCache!!))
            }
        }
    }

    fun getStyleForDisplayedPath(fontPath: FontPath): InspFontStyle {
        return if (fontPath == currentFontPath.value)
            currentFontStyle.value
        else
            InspFontStyle.regular
    }


}

private suspend fun getUploadedFontsAsync(uploadedFontsProvider: UploadedFontsProvider) =
    withContext(Dispatchers.Default) {
        uploadedFontsProvider.getFonts()
    }