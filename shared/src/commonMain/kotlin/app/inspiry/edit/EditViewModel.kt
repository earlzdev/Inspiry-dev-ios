package app.inspiry.edit

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.data.*
import app.inspiry.core.data.templateCategory.TemplateCategoryProvider
import app.inspiry.core.database.ExternalResourceDao
import app.inspiry.core.log.GlobalLogger
import app.inspiry.core.manager.AppViewModel
import app.inspiry.core.manager.DebugManager
import app.inspiry.core.manager.InspRemoteConfig
import app.inspiry.core.manager.LicenseManager
import app.inspiry.core.media.*
import app.inspiry.core.notification.FreeWeeklyTemplatesNotificationManager
import app.inspiry.core.notification.StoryUnfinishedNotificationManager
import app.inspiry.core.serialization.MediaSerializer
import app.inspiry.core.template.MediaReadWrite
import app.inspiry.core.template.TemplateReadWrite
import app.inspiry.core.template.TemplateViewModel
import app.inspiry.core.util.PickMediaResult
import app.inspiry.core.util.collectUntil
import app.inspiry.edit.instruments.InstrumentAdditional
import app.inspiry.edit.instruments.InstrumentsManager
import app.inspiry.edit.instruments.PickImageConfig
import app.inspiry.edit.instruments.PickedMediaType
import app.inspiry.font.helpers.TextCaseHelper
import app.inspiry.font.provider.PlatformFontPathProvider
import app.inspiry.font.provider.UploadedFontsProvider
import app.inspiry.music.model.TemplateMusic
import app.inspiry.preview.viewmodel.PreviewViewModel
import app.inspiry.removebg.RemovingBgViewModel
import app.inspiry.views.InspView
import app.inspiry.views.group.InspGroupView
import app.inspiry.views.media.InspMediaView
import app.inspiry.views.template.*
import app.inspiry.views.text.InspTextView
import app.inspiry.views.vector.InspVectorView
import com.russhwolf.settings.Settings
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import dev.icerock.moko.permissions.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class EditViewModel(
    private val licenseManger: LicenseManager,
    private val templateCategoryProvider: TemplateCategoryProvider,
    private val templateViewModel: TemplateViewModel,
    private val freeWeeklyTemplatesNotificationManager: FreeWeeklyTemplatesNotificationManager,
    private val scope: CoroutineScope,
    val templateView: InspTemplateView,
    private val appViewModel: AppViewModel,
    private val storyUnfinishedNotificationManager: StoryUnfinishedNotificationManager,
    private val templateSaver: TemplateReadWrite,
    private val mediaReadWrite: MediaReadWrite,
    var templatePath: TemplatePath,
    private val initialOriginalTemplateData: OriginalTemplateData?,
    val externalResourceDao: ExternalResourceDao,
    private val settings: Settings,
    private val remoteConfig: InspRemoteConfig,
    val analyticsManager: AnalyticsManager,
    platformFontPathProvider: PlatformFontPathProvider,
    uploadedFontsProvider: UploadedFontsProvider,
    textCaseHelper: TextCaseHelper
) : ViewModel() {

    var dontSaveChangesOnStop = false

    var instrumentsManager: InstrumentsManager
    val templateFormatState = MutableStateFlow<TemplateFormat?>(null)
    private var loadTemplateJob: Job? = null

    val templateFormat: TemplateFormat
        get() = templateFormatState.value ?: TemplateFormat.story

    val template = templateViewModel.template

    val editScreenState = MutableStateFlow(EditScreenState.EDIT)

    val isKeyboardShown: MutableStateFlow<Boolean> = MutableStateFlow(false)

    var previewViewModel: PreviewViewModel? = null

    init {
        templateView.textViewsAlwaysVisible = true

        instrumentsManager =
            InstrumentsManager(
                scope,
                templateView,
                analyticsManager,
                licenseManger,
                templateView.json,
                templateView.fontsManager,
                uploadedFontsProvider,
                textCaseHelper,
                platformFontPathProvider,
                remoteConfig,
                settings
            )

        instrumentsManager.setFormatChangedAction { new ->
            templateFormatState.value = new
        }

        templateInitializedCallbacks()

        templateView.onTemplateEditAction { templateEditIntent, inspView ->
            when (templateEditIntent) {
                TemplateEditIntent.PICK_IMAGE -> {
                    pickMediaConfigure(inspView)
                    instrumentsManager.pickImage()
                }
                TemplateEditIntent.TEXT_EDIT -> {
                    isKeyboardShown.value = true
                }
                else -> {
                    throw IllegalStateException("not implemented")
                }
            }
        }
    }

    fun editTextDone(inspView: InspTextView, newText: String) {
        inspView.setNewText(newText)
        isKeyboardShown.value = false
    }

    fun toEditMode() {
        templateView.stopPlaying()
        templateView.textViewsAlwaysVisible = true
        templateView.templateMode = TemplateMode.EDIT
        editScreenState.value = EditScreenState.EDIT
        templateView.setFrameForEdit()
    }

    fun toPreviewMode() {
        templateView.changeSelectedView(null)
        previewViewModel = PreviewViewModel(
            licenseManger,
            templateCategoryProvider,
            settings,
            templateView
        )
        templateView.fillEmptyMedias()
        templateView.templateMode = TemplateMode.PREVIEW
        previewViewModel?.updatePreview(templateView.template, templatePath)
        editScreenState.value = EditScreenState.PREVIEW
        templateView.textViewsAlwaysVisible = false
        if (templateView.isPlaying.value) templateView.stopPlaying()
        templateView.startPlaying(resetFrame = true)
    }

    fun toRenderMode() {
        templateView.changeSelectedView(null)
        if (templateView.isPlaying.value) templateView.stopPlaying()
        templateView.templateMode = TemplateMode.PREVIEW
        templateView.textViewsAlwaysVisible = false
    }

    fun appendNewSlide() {
        val selected = templateView.selectedView ?: return
        if (selected.isInSlides()) {
            GlobalLogger.debug("editvm") {"slide append.."}
        }
    }

    val isInEditMode: Boolean
        get() = editScreenState.value == EditScreenState.EDIT

    fun setDemoToAllImages() {
        templateView.setDemosAsImages()
    }

    private fun templateIsChanged() = templateView.isChanged.value

    private fun savingConfirmationIsNeed(): Boolean {
        return templatePath !is UserSavedTemplatePath
    }

    fun backAction(): Boolean { //true if need return to previous screen
        if (editScreenState.value != EditScreenState.EDIT) {
            toEditMode()
            return false
        }
        if (instrumentsManager.additionalPanelOpened()) {
            instrumentsManager.mayRemoveAdditionalPanel()
            return false
        }
        if (templateView.selectedView != null) {
            templateView.changeSelectedView(null)
            return false
        }
        return true
    }

    fun removeBottomPanel() {
        instrumentsManager.mayRemoveAdditionalPanel()
    }

    fun setMusic(music: TemplateMusic?) {
        whenTemplateInitializedCancelable {
            if (music != null) {
                templateView.setNewMusic(music)
                instrumentsManager.updateInnerInstrument(newState = InstrumentAdditional.EDIT_MUSIC)
            } else if (templateView.template.music == null) instrumentsManager.closeInnerPanel()
        }
    }

    fun getTopBarActionDisplayFlow(): Flow<EditTopbarAction> {
        return getDisplayWatermarkFlow().combine(instrumentsManager.isCloseablePanelOpened) { flow1, flow2 ->

            when {
                flow2 -> EditTopbarAction.DONE
                flow1 -> EditTopbarAction.SUBSCRIBE
                else -> EditTopbarAction.EXPORT
            }
        }
    }

    /**
     * @param permissionsController is nonnull when we need to get permission before export
     */
    suspend fun exportButtonClick(
        startRenderWithoutPurchase: Boolean,
        permissionsController: PermissionsController?
    ): ExportAction? {

        val innerExport =
            exportAction(startRenderWithoutPurchase)


        if (innerExport is ExportActionSave) {
            saveTemplateToFile().join()
            innerExport.templatePath = templatePath
        }

        if (innerExport is ExportActionSave && permissionsController != null &&
            !permissionsController.isPermissionGranted(Permission.WRITE_STORAGE)
        ) {

            try {
                permissionsController.providePermission(Permission.WRITE_STORAGE)
                return innerExport

            } catch (deniedAlways: DeniedAlwaysException) {
                // Permission is always denied.
            } catch (denied: DeniedException) {
                // Permission was denied.
            } catch (e: RequestCanceledException) {

            }
            return null
        } else {
            return innerExport
        }
    }

    private fun exportAction(startRenderWithoutPurchase: Boolean): ExportAction {
        val changedTemplate = templateView.template

        if (!startRenderWithoutPurchase && !changedTemplate.availableForUser(
                licenseManger.hasPremiumState.value, templatePath,
                templateCategoryProvider
            )
        ) {
            return ExportActionForPremium("share_template")

        } else {

            val isStatic = templateView.isStatic()
            val originalTemplateData = changedTemplate.originalData!!

            return ExportActionSave(isStatic, templatePath, originalTemplateData)
        }
    }
    private fun onGotSticker(media: Media) { //ios
        instrumentsManager.resetFullScreenTool()
        val defaultColor = templateView.template.palette.defaultTextColor
        mediaReadWrite.processSticker(media, defaultColor)
        templateView.addMediaContent(media)
    }
    private suspend fun onGotSticker(content: String) {
        instrumentsManager.resetFullScreenTool()
        val sticker = withContext(Dispatchers.Default) {
            val defaultColor = templateView.template.palette.defaultTextColor

            val argumentIsContentElsePath = true

            if (argumentIsContentElsePath) {
                val media: Media =
                    mediaReadWrite.json.decodeFromString(MediaSerializer, content)
                mediaReadWrite.processSticker(media, defaultColor)
                media
            } else {

                mediaReadWrite.openAndProcessSticker(content, defaultColor)
            }
        }
        templateView.addMediaContent(sticker)
    }

    suspend fun onTextPicked(
        returnTextHere: InspTextView?,
        textAnimationResult: String?
    ) {
        if (textAnimationResult != null) {
            whenTemplateInitializedCancelable {
                val model = withContext(Dispatchers.Default) {
                    mediaReadWrite.openMediaTextAfterSelection(
                        textAnimationResult,
                        templateView.template.palette.defaultTextColor
                    )
                }
                templateView.applyStyleToText(returnTextHere, model)
            }
        }
    }


    fun whenTemplateInitializedCancelable(action: suspend () -> Unit) {

        if (!templateView.isInitialized.value) {

            scope.collectUntil(templateView.isInitialized, {
                it
            }, action = action)


        } else {
            scope.launch {
                action()
            }
        }
    }

    fun loadTemplatePath() {
        templateViewModel.loadTemplate(templatePath)
        scope.launch {
            templateViewModel.template.collect {
                if (it is InspResponseData) onTemplateDataLoaded(it.data)
            }
        }
    }

    fun setTemplateCenterGravity(value: Float) {
        templateView.setTemplateTransform(templateView.templateTransform.value.copy(centerGravity = value))
    }

    fun onTemplateDataLoaded(t: Template) {
        val originalDataIntent = initialOriginalTemplateData

        if (originalDataIntent == null && t.originalData == null) throw IllegalStateException()
        else if (t.originalData == null)
            t.originalData = originalDataIntent

        if (templateFormatState.value == null) {
            templateFormatState.value = t.format
        }
        templateView.stopPlaying()
        templateView.loadTemplate(t)
    }

    fun canPickVideoForImage(view: InspView<*>?): Boolean {
        return view?.isSocialIcon() != true
    }

    fun getMaxSelectableCount(view: InspView<*>?): Int {
        var maxSelectable: Int

        if (view == null || view.isSocialIcon()) {
            maxSelectable = 1
        } else {
            maxSelectable =
                view.templateParent.getSelectableMediaViews().sumOf { it.sourceCount }
            if (maxSelectable == 0) maxSelectable++
        }
        return maxSelectable
    }

    fun saveTemplateToFile(): Job {
        if (templateViewModel.template.value == null) {
            throw IllegalStateException("too early")
        }

        templateView.isChanged.value = false
        val t = templateView.template

        return appViewModel.applicationScope.launch {

            val newFile = withContext(Dispatchers.Default) {
                templateSaver.saveTemplateToFile(t, templatePath)
            }

            templatePath = newFile
            storyUnfinishedNotificationManager.onStorySaved(newFile.path)
        }
    }
    fun saveTemplateToFile(onSaveFinished: (() -> Unit)?) {
        saveTemplateToFile().invokeOnCompletion { onSaveFinished?.invoke() }
    }

    fun templateInitializedCallbacks() {
        loadTemplateJob?.cancel()
        loadTemplateJob = scope.launch {
            templateView.isInitialized.collect {
                if (it) {
                    templateFormatState.value?.let { templateView.changeFormat(it) }
                    GlobalLogger.debug("editViewModel") {"set frame for edit current frame = ${templateView.currentFrame}"}
                    templateView.setFrameForEdit()
                    GlobalLogger.debug("editViewModel") {"set frame for edit new frame = ${templateView.currentFrame}"}
                    GlobalLogger.debug("editViewModel") {"text always visible = ${templateView.textViewsAlwaysVisible}"}
                    instrumentsManager.maybeUpdateInstruments(templateView.selectedView)
                }
            }
        }
    }

    //if true then display
    fun getDisplayWatermarkFlow(): Flow<Boolean> {

        return combine(
            licenseManger.hasPremiumState,
            templateViewModel.template,
            freeWeeklyTemplatesNotificationManager.currentWeekIndex
        ) { hasPremium, template, weekIndex ->

            if (template !is InspResponseData<Template>)
                false
            else {
                !template.data.availableForUser(hasPremium, templatePath, templateCategoryProvider)
            }
        }
    }

    fun displayWatermarkCollector(onChange: (Boolean) -> Unit) {
        val flow = getDisplayWatermarkFlow()
        scope.launch {
            flow.collect {
                onChange(it)
            }
        }
        //return flow.first()
    }


    fun onStickerResult(it: String?) {
        whenTemplateInitializedCancelable {
            if (it != null) {

                instrumentsManager.maybeUpdateInstruments(templateView.selectedView)
                onGotSticker(it)
            }
        }
    }
    fun onStickerResult(media: Media?) {
        whenTemplateInitializedCancelable {
            if (media != null) {
                instrumentsManager.maybeUpdateInstruments(templateView.selectedView)
                onGotSticker(media)
            }
        }
    }

    fun insertRemovedBgView(mediaView: InspMediaView? = null, resultItem: PickMediaResult) {
        val viewThatWasSelected = (mediaView ?: templateView.selectedView as? InspMediaView)
            ?: throw IllegalStateException("removebg not media or null")
        templateView.selectedView = null
        if (viewThatWasSelected.media.makeMovableWhenRemoveBg) {
            viewThatWasSelected.turnToRemovedBgView(resultItem.size)
            viewThatWasSelected.doForDuplicated { it.turnToRemovedBgView(resultItem.size) }
        }
        // in order to refresh action buttons
        viewThatWasSelected.onNewImagePicked(resultItem.uri, selectAfterLoad = true)
    }

    fun showConfirmationIsNeed(confirmed: Boolean, onSaveFinished: ((Boolean) -> Unit)? = null): Boolean {
        if (templateIsChanged()) {
            if (savingConfirmationIsNeed() && !confirmed) {
                return true
            } else {
                saveTemplateToFile().invokeOnCompletion { onSaveFinished?.invoke(true) }
                return false
            }
        }
        onSaveFinished?.invoke(false)
        return false
    }

    fun onExitWithoutSave() {

        val template = templateView.template
        val templatePath = templatePath
        dontSaveChangesOnStop = true
        // delete only if it wasn't saved
        if (templatePath is PredefinedTemplatePath) {
            appViewModel.applicationScope.launch(Dispatchers.Default) {
                templateViewModel.templateReadWrite.deleteTemplateFiles(
                    template,
                    templatePath,
                    externalResourceDao
                )
            }
        }
    }

    fun saveOnStop() = !dontSaveChangesOnStop && templateView.isChanged.value

    fun canRemoveBgOrOpenPromo(hasPremium: Boolean = licenseManger.hasPremiumState.value): Boolean {
        return if (hasPremium || DebugManager.isDebug)
            true
        else {
            val removeBgTimes = settings.getInt(RemovingBgViewModel.KEY_NUM_PROCESSED_IMAGES)
            removeBgTimes < remoteConfig.getInt("remove_bg_free_tries")
        }
    }

    fun insertRemovedBgBatch(resultItem: List<PickMediaResult>) {
        val medias = getMediaViewsToRemoveBgAfterPickedFromGallery()
        for ((index, media) in medias.withIndex()) {
            val res = resultItem.getOrNull(index) ?: continue
            insertRemovedBgView(media, res)
        }
    }

    fun getMediaViewsToRemoveBgAfterPickedFromGallery(): List<InspMediaView> {
        //return if (!remoteConfig.getBoolean("remove_bg_instantly_after_gallery")) emptyList()
        //else
        return templateView.mediaViews.filter {
            (it.media.removeBgOnInsert || it.media.undoRemoveBgData != null) &&
                    it.media.originalSource != null &&
                    !it.isVideo()
        }
    }


    private var _pickImageConfigHolder: PickImageConfig? = null

    fun getPickImageConfig(): PickImageConfig {
        if (_pickImageConfigHolder == null) {
            if (DebugManager.isDebug) throw IllegalStateException("pickImageConfigHolder is null!")
            pickMediaConfigure(null)
        }
        return _pickImageConfigHolder!!

    }

    fun setSingleMediaConfig() {
        pickMediaConfigure(templateView.selectedView, true)
    }

    fun pickMediaConfigure(view: InspView<*>?, isReplace: Boolean = false) {
        val resultViewIndex: Int
        var mediaResultType = PickedMediaType.MEDIA

        when  {
            view?.isInSlides() == true -> {
                resultViewIndex = templateView.mediaViews.indexOf(view)
            }
            view is InspMediaView -> {
                resultViewIndex = templateView.mediaViews.indexOf(view)
            }
            view is InspVectorView -> {
                resultViewIndex = templateView.allViews.indexOf(view)
                mediaResultType = PickedMediaType.VECTOR
            }
            else -> {
                resultViewIndex = -1
            }
        }

        var maxSelectable: Int

        //we choose only 1 image for social icons
        val isSocial = view?.isSocialIcon() ?: false

        if (view == null || isSocial || isReplace) {
            maxSelectable = 1
        } else {
            maxSelectable = if (view.isInSlides()) {
                (view.parentInsp as? InspGroupView)?.media?.slides?.maxCount ?: 1
            } else {
                view.templateParent.getSelectableMediaViews().sumOf { it.sourceCount }
            }
            if (maxSelectable == 0) maxSelectable++
        }
        _pickImageConfigHolder = PickImageConfig(
            maxSelectable = maxSelectable,
            imageOnly = isSocial,
            pickedMediaType = mediaResultType,
            resultViewIndex = resultViewIndex,
            replaceMedia = isReplace
        )
    }

    private fun setImageToVectorMedia(path: String, config: PickImageConfig) {

        val oldView = templateView.allViews[config.resultViewIndex]
        templateView.replaceVectorWithMedia(oldView, path)
        templateView.changeSelectedView(null)

    }
    var mediasToRemoveBg: List<InspMediaView>? = null
    fun onImagePicked(result: List<PickMediaResult>, onAdded: () -> Unit = {}) {
        val config = getPickImageConfig()

        val viewThatWasSelected =
            templateView.mediaViews.getOrNull(config.resultViewIndex)

        if (viewThatWasSelected == null) {
            onMediaPicked(result, isLogo = true)
            return
        }

        if (config.pickedMediaType == PickedMediaType.VECTOR &&
            result.isNotEmpty()
        ) setImageToVectorMedia(result[0].uri, config)

        if (!viewThatWasSelected.isInSlides() || config.replaceMedia) {
        templateView.loadBunchMedias(
            viewThatWasSelected, result, analyticsManager
        )
        } else {
            val slidesParent = viewThatWasSelected.getSlidesParent()

            analyticsManager.onSlideAdded(slidesParent.getSlidesCount(ignorePlaceHolder = true) + result.size)
            templateView.slideUtilities.replaceSlides(slidesParent, result) {
                onAdded()
                slidesParent.selectFirstSlide()
            }

        }

        mediasToRemoveBg = getMediaViewsToRemoveBgAfterPickedFromGallery()
        if (mediasToRemoveBg?.isNotEmpty() == true) {
            if (canRemoveBgOrOpenPromo()) {
                //val list = arrayListOf<String>()
                //mediasToRemoveBg.mapTo(list) { it.media.originalSource!! }
                instrumentsManager.removeBGAction()

            } else {
                instrumentsManager.removeBGPromo()
            }
        }
    }

    fun onMediaPicked(result: List<PickMediaResult>, isLogo: Boolean = false) {

        if (result.isNotEmpty()) {
            val image = MediaImage.getLogoOrImageFromPath(result[0])
            image.isLogo = isLogo
            templateView.addMediaContent(image)
        }
    }
}

enum class EditScreenState {
    EDIT, PREVIEW, RENDER
}
