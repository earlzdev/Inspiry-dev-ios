package app.inspiry.textanim

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.analytics.putBoolean
import app.inspiry.core.analytics.putString
import app.inspiry.core.data.FRAME_IN_MILLIS
import app.inspiry.core.log.ErrorHandler
import app.inspiry.core.log.KLogger
import app.inspiry.core.log.LoggerGetter
import app.inspiry.core.media.BaseUnitsConverter
import app.inspiry.core.media.Media
import app.inspiry.core.media.Template
import app.inspiry.core.util.getFileName
import app.inspiry.core.util.removeExt
import app.inspiry.font.helpers.TextCaseHelper
import app.inspiry.views.template.InspTemplateView
import dev.icerock.moko.resources.AssetResource
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json

class TextAnimViewModel(
    var currentText: String?,
    private val textCaseHelper: TextCaseHelper,
    private val json: Json,
    private val unitsConverter: BaseUnitsConverter,
    private val provider: TextAnimProvider,
    private val analyticsManager: AnalyticsManager,
    private val errorHandler: ErrorHandler,
    loggerGetter: LoggerGetter,
    initialTabNum: Int,
    initialAnimationPath: String?
) : dev.icerock.moko.mvvm.viewmodel.ViewModel() {

    val selectedAnimationPath: AssetResource?
        get() = currentPreviewAnimation.value?.res

    private val logger: KLogger = loggerGetter.getLogger("TextAnimViewModel")
    private var previewAnimationJobs: MutableMap<InspTemplateView, Job> = mutableMapOf()

    private val originalTabs = provider.getCategories()
    val currentAnimations = MutableStateFlow<List<MediaWithRes>?>(null)
    val currentPreviewAnimation = MutableStateFlow<MediaWithRes?>(null)
    val currentTabNum = MutableStateFlow(initialTabNum)

    init {
        showAnimationsFromTab(initialTabNum, true, initialAnimationPath)
    }

    fun initPreviewTemplateView(templateView: InspTemplateView) {

        templateView.run {
            template = Template()
            shouldHaveBackground = false
        }
    }

    private fun MediaWithRes.copyForPreview(): MediaWithRes = MediaWithRes(copyForPreview(media), res)

    private fun copyForPreview(media: Media) = media.copy(json).also {

        val t = it.selectTextView()
        if (t != null) {
            if (t.id == "socialText") t.multiplyTextSize(
                unitsConverter,
                SOCIAL_TEXT_SIZE_IN_PREVIEW_MULTIPLIER
            )
            else t.multiplyTextSize(unitsConverter, TEXT_SIZE_IN_PREVIEW_MULTIPLIER)

            val currentText = currentText
            if (currentText != null) {
                t.text = textCaseHelper.setCaseBasedOnOther(currentText, t.text)
            }
        }
    }

    fun previewAnimationInList(media: Media, position: Int, templateView: InspTemplateView) {
        previewAnimationWithDelay(
            media,
            templateView,
            if (position % 2 == 0) 0 else (FRAME_IN_MILLIS * 10).toLong()
        )
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    fun showAnimationsFromTab(
        tab: Int,
        previewFirstTemplate: Boolean,
        forPreviewMediaPath: String? = null
    ) {
        currentTabNum.value = tab

        viewModelScope.launch {

            try {

                val list = withContext(Dispatchers.Default) {
                    provider.getAnimations(originalTabs[tab])
                }

                currentAnimations.emit(list)

                if (previewFirstTemplate) {

                    var index: Int = -1
                    if (forPreviewMediaPath != null) {
                        index = list.indexOfFirst { it.res.originalPath == forPreviewMediaPath }
                        if (index != -1) {
                            val data = list[index]
                            currentPreviewAnimation.emit(data.copyForPreview())
                        }
                    }
                    if (index == -1)
                        currentPreviewAnimation.emit(list.first().copyForPreview())
                }

            } catch (e: Exception) {
                logger.error(e)
                errorHandler.toastError(e)
            }
        }
    }

    fun shouldOpenSubscribeOnClickSave(
        hasPremium: Boolean,
        templateView: InspTemplateView
    ): Boolean {
        return templateView.template.medias.first()
            .forPremium && !hasPremium
    }

    fun onClickTemplateInList(data: MediaWithRes) {
        currentPreviewAnimation.value = MediaWithRes(copyForPreview(data.media), data.res)
    }

    fun previewAnimation(media: Media, templateView: InspTemplateView) {
        previewAnimationJobs.remove(templateView)

        templateView.removeViews()
        templateView.template.medias.clear()
        templateView.template.medias.add(media)

        //TODO: maybe check children minDuration
        if (media.minDuration == Media.MIN_DURATION_AS_TEMPLATE)
            templateView.template.preferredDuration = 10000
        else
            templateView.template.preferredDuration = 0

        templateView.loadTemplate(templateView.template)
        templateView.startPlaying()
    }

    private fun previewAnimationWithDelay(
        media: Media, templateView: InspTemplateView,
        delay: Long
    ) {
        templateView.removeViews()

        previewAnimationJobs.remove(templateView)?.cancel()
        previewAnimationJobs[templateView] = viewModelScope.launch {

            delay(delay)

            if (!isActive) {
                return@launch
            }
            previewAnimation(media, templateView)
        }
    }

    fun onClickSaveTemplate() {
        val currentPreviewAnim = currentPreviewAnimation.value
        if (currentPreviewAnim != null) {
            val media = currentPreviewAnim.media

            analyticsManager.sendEvent("text_animation_picked", createParams = {
                putString(
                    "animation_name", currentPreviewAnim.res.originalPath.getFileName().removeExt()
                )
                putString("animation_category", categoryNumToString(currentTabNum.value))
                putBoolean("is_premium", media.forPremium)
            })
        }
    }

    private fun categoryNumToString(num: Int): String {
        return getTextAnimationTabs()[num]
    }

    fun getTextAnimationTabs(): MutableList<String> {
        return mutableListOf("Title", "Caption", "Minimal", "Brush", "Swipe Up", "Social")
    }


    companion object {
        const val TEXT_SIZE_IN_PREVIEW_MULTIPLIER = 0.6f
        const val SOCIAL_TEXT_SIZE_IN_PREVIEW_MULTIPLIER = 0.9f
    }

}