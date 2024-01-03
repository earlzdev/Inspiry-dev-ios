package app.inspiry.edit.instruments

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.log.GlobalLogger
import app.inspiry.core.manager.InspRemoteConfig
import app.inspiry.core.manager.LicenseManager
import app.inspiry.core.media.TemplateFormat
import app.inspiry.slide.getAvailableTypesForVideo
import app.inspiry.core.util.createDefaultScope
import app.inspiry.edit.colorChanging.ColorDialogViewModelProvider
import app.inspiry.edit.instruments.BottomMenuItems.menuAddViews
import app.inspiry.edit.instruments.color.ColorDialogViewModel
import app.inspiry.edit.instruments.defaultPanel.DefaultInstrumentsPanelViewModel
import app.inspiry.views.InspView
import app.inspiry.views.media.InspMediaView
import app.inspiry.views.template.InspTemplateView
import app.inspiry.views.text.InspTextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import app.inspiry.edit.instruments.defaultPanel.DefaultInstruments.*
import app.inspiry.edit.instruments.font.FontsViewModel
import app.inspiry.edit.instruments.format.FormatSelectorViewModel
import app.inspiry.edit.instruments.media.MediaInstrumentsPanelViewModel
import app.inspiry.edit.instruments.textPanel.TextInstruments.*
import app.inspiry.edit.instruments.textPanel.TextInstrumentsPanelViewModel
import app.inspiry.edit.instruments.textPanel.TextSizeInstrumentViewModel
import app.inspiry.edit.instruments.InstrumentAdditional.*
import app.inspiry.edit.instruments.addViewsPanel.*
import app.inspiry.edit.instruments.media.VideoEditViewModel
import app.inspiry.edit.instruments.moveAnimPanel.MoveAnimInstrumentModel
import app.inspiry.edit.instruments.shapes.ShapesInstrumentViewModel
import app.inspiry.edit.socialIconsSelector.SocialIconsViewModel
import app.inspiry.font.helpers.TextCaseHelper
import app.inspiry.font.provider.FontsManager
import app.inspiry.font.provider.PlatformFontPathProvider
import app.inspiry.font.provider.UploadedFontsProvider
import app.inspiry.slide.MediaInstrumentType
import app.inspiry.slide.model.SlideInstrumentViewModel
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.*
import kotlin.jvm.JvmOverloads

class InstrumentsManager
@JvmOverloads constructor(
    val scope: CoroutineScope = createDefaultScope(),
    val templateView: InspTemplateView,
    val analyticsManager: AnalyticsManager,
    val licenseManager: LicenseManager,
    val json: Json,
    val fontsManager: FontsManager,
    val uploadedFontsProvider: UploadedFontsProvider,
    val textCaseHelper: TextCaseHelper,
    val platformFontPathProvider: PlatformFontPathProvider,
    val remoteConfig: InspRemoteConfig,
    val settings: Settings
) {

    fun additionalPanelOpened() = isCloseablePanelOpened.value

    init {
        scope.launch {
            templateView.selectedViewState.collect {
                if (templateView.isInitialized.value) {
                    maybeUpdateInstruments(it)
                }
            }
        }
    }



    fun resetFullScreenTool() {
        fullScreenTools.value = null
    }

    fun selectFullScreenTool(newState: FullScreenTools, closePanel: Boolean = true) {
        if (closePanel) closeInnerPanel()
        fullScreenTools.value = newState
    }

    /**
     * getting viewModel from model holder
     *
     * fixme: there is a lot of duplicated code here
     * the method of getting the model should be simplified
     * but we need to be careful with GENERIC and INLINE
     * because they don't work well on ios.
     */

    fun getAddViewsModel(): AddViewsPanelModel? {
        return modelHolder.getMainElseAdditional()
    }

    fun getDefaultModel(): DefaultInstrumentsPanelViewModel? {
        return modelHolder.getMainElseAdditional()
    }

    fun getColorModel(): ColorDialogViewModel? {
        return modelHolder.getMainElseAdditional()
    }

    fun getFormatModel(): FormatSelectorViewModel? {
        return modelHolder.getMainElseAdditional()
    }

    fun getFontModel(): FontsViewModel? {
        return modelHolder.getMainElseAdditional()
    }

    fun getSizeModel(): TextSizeInstrumentViewModel? {
        return modelHolder.getMainElseAdditional()
    }

    fun getTimeLineModel(): TimeLineInstrumentModel? {
        return modelHolder.getMainElseAdditional()
    }

    fun getMediaModel(): MediaInstrumentsPanelViewModel? {
        return modelHolder.getMainElseAdditional()
    }

    fun getVideoEditModel(): VideoEditViewModel? {
        return modelHolder.getMainElseAdditional()
    }

    fun getSocialModel(): SocialIconsViewModel? {
        return modelHolder.getMainElseAdditional()
    }

    fun getTextModel(): TextInstrumentsPanelViewModel? {
        return modelHolder.getMainElseAdditional()
    }

    fun getShapesModel(): ShapesInstrumentViewModel? {
        return modelHolder.getMainElseAdditional()
    }
    fun getMoveModel(): MoveAnimInstrumentModel? {
        return modelHolder.getMainElseAdditional()
    }
    fun getSlidesModel(): SlideInstrumentViewModel? {
        return modelHolder.getMainElseAdditional()
    }

    private var formatChangedAction: ((TemplateFormat) -> Unit)? = null
    fun setFormatChangedAction(action: (TemplateFormat) -> Unit) {
        formatChangedAction = action
    }


    private fun initMediaInstruments(selectedView: InspView<*>?): Boolean {
        if (selectedView is InspMediaView) {
            val model = MediaInstrumentsPanelViewModel(
                selectedView,
                analyticsManager,
                licenseManager
            )
            model.setOnSelect { mediaInstrumentType, inspMediaView ->
                when (mediaInstrumentType) {
                    MediaInstrumentType.REMOVE_BG -> removeBGAction()
                    MediaInstrumentType.VOLUME -> updateInnerInstrument(inspMediaView, VOLUME)
                    MediaInstrumentType.TRIM -> updateInnerInstrument(inspMediaView, TRIM)
                    MediaInstrumentType.COLOR -> updateInnerInstrument(inspMediaView, COLOR)
                    MediaInstrumentType.REPLACE -> selectFullScreenTool(FullScreenTools.REPLACE)
                    MediaInstrumentType.SHAPE -> updateInnerInstrument(inspMediaView, SHAPE)
                    MediaInstrumentType.MOVE -> updateInnerInstrument(inspMediaView, MOVE)
                    MediaInstrumentType.SLIDE -> updateInnerInstrument(inspMediaView, SLIDE)
                    null -> {
                        mayRemoveAdditionalPanel()
                        updateInnerInstrument(null, null)
                    }
                    else -> {}
                }
            }
            modelHolder.setTabsModel(
                model
            )
            if (selectedView.isInSlides()) {
            selectedView.getSlidesParent().let {
                val slidesCount = it.getSlidesCount(true)
                if (slidesCount <= 0) return true
                if (slidesCount == 1 && it.getSlidesViews().first().isVideo()) {
                    model.selectInstrument(MediaInstrumentType.TRIM)
                    return false
                } else {

                    model.selectInstrument(MediaInstrumentType.SLIDE)
                    return false
                }
            }
            } else {
                if (selectedView.isVideo()) {
                    model.selectInstrument(MediaInstrumentType.TRIM)
                    return false
                }
            }

        } else throw IllegalStateException("invalid InspView type (must be InspMediaView), id: ${selectedView?.media?.id}")
        return true

    }

    private fun initSlidesModel(selectedView: InspView<*>?) {
        if (selectedView is InspMediaView) {
            modelHolder.setAdditionalModel(
                SlideInstrumentViewModel(inspView = selectedView, analyticsManager)
            )
        }
    }

    private fun initVideoEditModel(selectedView: InspView<*>?) {
        if (selectedView is InspMediaView) {
            modelHolder.setAdditionalModel(
                VideoEditViewModel(inspView = selectedView)
            )
        }
    }

    private fun initShapesModel(selectedView: InspView<*>?) {
        if (selectedView is InspMediaView) {
            modelHolder.setAdditionalModel(ShapesInstrumentViewModel(selectedView, analyticsManager))
        }
    }
    private fun initMoveModel(selectedView: InspView<*>?) {
        if (selectedView is InspMediaView) {
            modelHolder.setAdditionalModel(MoveAnimInstrumentModel(selectedView, analyticsManager))
        }
    }

    private fun initTimeLineInstruments(selectedView: InspView<*>?) {
        modelHolder.setAdditionalModel(
            TimeLineInstrumentModel(templateView = templateView, currentView = selectedView)
        )
    }

    private fun initFontInstruments(selectedView: InspView<*>?) {
        if (selectedView is InspTextView) modelHolder.setAdditionalModel(
            FontsViewModel(
                selectedView,
                fontsManager,
                uploadedFontsProvider,
                analyticsManager,
                textCaseHelper,
                licenseManager,
                platformFontPathProvider,
                onSubscribe = {
                    selectFullScreenTool(FullScreenTools.SUBSCRIBE, false)
                }
            )
        )
        else throw IllegalStateException("invalid InspView type (must be InspTextView), id: ${selectedView?.media?.id}")
    }

    private fun initColorInstruments(selectedView: InspView<*>?, isBack: Boolean = false) {
        modelHolder.setAdditionalModel(
            ColorDialogViewModelProvider(
                templateView = templateView,
                selectedView,
                isBack,
                analyticsManager,
                json
            ).create().apply {
                initDefaults()
            }
        )
    }

    private fun initFormatInstruments() {
        modelHolder.setAdditionalModel(
            FormatSelectorViewModel(templateView, licenseManager) {
                if (it != null) formatChangedAction?.invoke(it)
                else selectFullScreenTool(FullScreenTools.SUBSCRIBE, false)
            }
        )
    }

    private fun initSocialIconInstruments(selectedView: InspView<*>) {
        modelHolder.setAdditionalModel(
            SocialIconsViewModel(inspView = selectedView)
        )
    }

    private fun initAddViewsTabsModel() {

        val model = AddViewsPanelModel(
            analyticsManager, menuAddViews
        )

        model.onSelectedAction {
            when (it) {
                AddViewsInstruments.ADD_TEXT -> {
                    selectFullScreenTool(FullScreenTools.TEXT_ANIM)
                }
                AddViewsInstruments.ADD_STICKER -> {
                    selectFullScreenTool(FullScreenTools.STICKERS)
                }
                AddViewsInstruments.ADD_LOGO -> {
                    selectFullScreenTool(FullScreenTools.ADD_LOGO)
                }
                AddViewsInstruments.ADD_FRAME -> {
                    selectFullScreenTool(FullScreenTools.PICK_SINGLE_MEDIA)
                }
                null -> {
                    mayRemoveAdditionalPanel()
                    updateInnerInstrument(null, null)
                }
            }
        }
        modelHolder.setTabsModel(model)
    }

    private fun initDefaultTabsModel() {
        val model = DefaultInstrumentsPanelViewModel(
            templateView,
            analyticsManager = analyticsManager,
            licenseManager = licenseManager,
            json = json, BottomMenuItems.getMenuDefault(templatePaletteEnabled())
        )

        model.onSelectedAction {
            when (it) {
                DEFAULT_ADD -> {
                    updateInstruments(null, InstrumentMain.ADD_VIEWS)
                }
                DEFAULT_COLOR -> {
                    updateInnerInstrument(null, COLOR)
                }
                DEFAULT_FORMAT -> {
                    updateInnerInstrument(null, FORMAT)
                }
                DEFAULT_LAYERS -> {
                    updateInstruments(null, InstrumentMain.TIMELINE)
                }
                DEFAULT_MUSIC -> {
                    if (templateView.template.music == null) selectFullScreenTool(
                        FullScreenTools.MUSIC,
                        false
                    )
                    else updateInnerInstrument(null, EDIT_MUSIC)
                }
                DEFAULT_DEBUG -> {
                    updateInstruments(null, newState = InstrumentMain.DEBUG)
                }
                null -> {
                    mayRemoveAdditionalPanel()
                    updateInnerInstrument(null, null)
                }
            }
        }
        modelHolder.setTabsModel(model)
    }

    private fun initTextTabsModel(selectedView: InspView<*>?) {
        val selected = selectedView as? InspTextView
            ?: throw IllegalStateException("not InspTextView view for text panel")

        val model =
            TextInstrumentsPanelViewModel(selected, analyticsManager, BottomMenuItems.menuText)
        model.setOnSelect { it, selectedText ->
            when (it) {
                TEXT_ANIMATION -> {
                    selectFullScreenTool(FullScreenTools.TEXT_ANIM)
                }
                TEXT_COLOR -> {
                    updateInnerInstrument(selectedText, COLOR)
                }
                TEXT_BACKGROUND -> {
                    updateInnerInstrument(selectedText, BACK)
                }
                TEXT_FONT -> {
                    updateInnerInstrument(selectedText, FONT)
                }
                TEXT_SIZE -> {
                    updateInnerInstrument(selectedText, SIZE)
                }
                TEXT_ALIGNMENT -> {
                }
                TEXT_LAYERS -> {
                    updateInstruments(selectedText, InstrumentMain.TIMELINE)
                }
                null -> {
                    mayRemoveAdditionalPanel()
                    updateInnerInstrument(null, null)
                }
            }
        }
        modelHolder.setTabsModel(model)
    }

    private fun initSizeInstruments(selectedView: InspView<*>?) {
        if (selectedView is InspTextView) modelHolder.setAdditionalModel(
            TextSizeInstrumentViewModel(selectedView, analyticsManager)
        )
        else throw IllegalStateException("invalid InspView type (must be InspTextView), id: ${selectedView?.media?.id}")
    }


    fun updateInnerInstrument(
        inspView: InspView<*>? = null,
        newState: InstrumentAdditional? = null
    ) {
        refreshInnerInstrumentModel(inspView, newState)
        instrumentAdditionalState = newState

    }

    fun mayRemoveAdditionalPanel() {
        if (!modelHolder.hasTabs() && currentInstrumentMain != InstrumentMain.TIMELINE) {
            templateView.changeSelectedView(null)
            return
        }
        updateInnerInstrument(null)
        if (currentInstrumentMain?.isCloseable() == true) {
            updateInstruments(
                templateView.selectedView,
                getCurrentInstruments(templateView.selectedView)
            )
        }
    }

    val instrumentsState = MutableStateFlow(EditInstrumentsState())

    val isCloseablePanelOpened: StateFlow<Boolean> =
        instrumentsState.map {
            it.currentAdditionalInstrument != null || it.currentMainInstrument?.isCloseable() == true
        }.stateIn(scope, SharingStarted.Lazily, false)

    val fullScreenTools: MutableStateFlow<FullScreenTools?> = MutableStateFlow(null)

    private var instrumentAdditionalState: InstrumentAdditional?
        get() = instrumentsState.value.currentAdditionalInstrument
        set(value) {
            instrumentsState.update { it.copy(currentAdditionalInstrument = value) }
        }

    private var currentInstrumentMain: InstrumentMain?
        get() = instrumentsState.value.currentMainInstrument
        set(value) {
            instrumentsState.update { it.copy(currentMainInstrument = value) }
        }

    private var currentSelectionType: SelectionType
        get() = instrumentsState.value.currentSelectionType
        set(value) {
            instrumentsState.update { it.copy(currentSelectionType = value) }
        }

    val modelHolder = BottomInstrumentModelHolder()

    fun removeBGAction() {
        fullScreenTools.value = FullScreenTools.REMOVE_BG
    }

    fun removeBGPromo() {
        fullScreenTools.value = FullScreenTools.REMOVE_BG_PROMO
    }

    fun saveButtonAction() {
        fullScreenTools.value = FullScreenTools.SAVING
    }

    private fun templatePaletteEnabled(): Boolean = templateView.template.palette.isAvailable

    private fun getCurrentInstruments(value: InspView<*>?): InstrumentMain {
        val newState: InstrumentMain = when {
            value is InspTextView -> {
                InstrumentMain.TEXT //text instruments
            }
            value?.isSocialIcon() == true -> {
                InstrumentMain.SOCIAL_ICONS //social icons panel
            }
            value is InspMediaView || showEditVideoInstrument() -> {
                InstrumentMain.MEDIA // video or image edit instruments
            }

            value?.media?.isMovable == true && !value.isColorChangeDisabled() -> {
                InstrumentMain.MOVABLE // instruments for movable

            }
            value?.isInSlides() == true -> {
                InstrumentMain.SLIDES // slides instrument (not implemented yet)
            }

            else -> InstrumentMain.DEFAULT // default panel
        }

        return newState
    }

    fun maybeUpdateInstruments(value: InspView<*>?) {

        // TODO: it would be better if timeline was observing changes by itself.
        if (currentInstrumentMain == InstrumentMain.TIMELINE) {
            modelHolder.updateSelection(value)
            return
        }

        val newState = getCurrentInstruments(value)
        updateInstruments(value, newState)

    }

    private fun selectionTypeChanged(value: InspView<*>?, newState: InstrumentMain?): Boolean {
        if (newState?.hasDifferentModel() == false) return false
        val newType = value?.getType() ?: SelectionType.NOTHING
        val oldType = currentSelectionType
        currentSelectionType = newType
        return newType != oldType
    }

    private fun updateInstruments(value: InspView<*>?, newState: InstrumentMain) {
        val typeChanged = selectionTypeChanged(value, newState)
        val willUpdate = newState != this.currentInstrumentMain || typeChanged
        if (willUpdate) {
            updateInnerInstrument(null)
            refreshMainInstrumentModel(value, newState)
            showCorrectInstruments(newState)
        } else {
            if (this.currentInstrumentMain?.instrumentsDependOnView() == true) {
                modelHolder.updateSelection(value)
            }
        }
    }

    private fun showEditVideoInstrument(): Boolean {
        val videoSelectedView = templateView.videoSelectedView

        return videoSelectedView != null && videoSelectedView.getAvailableTypesForVideo()
            .isNotEmpty()
    }

    /**
     * creating viewModels for bottom instruments
     */
    private fun refreshMainInstrumentModel(view: InspView<*>?, newState: InstrumentMain?) {
        newState?.let { instrument ->
            when (instrument) {
                InstrumentMain.ADD_VIEWS -> {
                    initAddViewsTabsModel() //init add view view
                }
                InstrumentMain.DEFAULT -> {
                    initDefaultTabsModel()
                }
                InstrumentMain.MOVABLE -> {
                    initColorInstruments(view)
                    modelHolder.removeTabsModel()
                }
                InstrumentMain.MEDIA -> {
                    if (initMediaInstruments(view)) {
                        instrumentAdditionalState = null
                        modelHolder.removeAdditionalModel()
                        } else { return@let  }
                }
                InstrumentMain.TEXT -> {
                    initTextTabsModel(view)
                    instrumentAdditionalState = null
                    modelHolder.removeAdditionalModel()
                }
                InstrumentMain.TIMELINE -> {
                    initTimeLineInstruments(view)
                    modelHolder.removeTabsModel()
                }
                InstrumentMain.SOCIAL_ICONS -> {
                    view?.let {
                        initSocialIconInstruments(it)
                        modelHolder.removeTabsModel()
                    }
                }
                InstrumentMain.SLIDES -> {
                    //todo
                }

                InstrumentMain.DEBUG -> {

                }
            }
        }
    }

    /**
     * creating view models for inner instruments
     */
    private fun refreshInnerInstrumentModel(view: InspView<*>?, newState: InstrumentAdditional?) {
        GlobalLogger.debug("InstrumentManager") {"newInnerInstrument ${newState?.name}"}
        newState?.let { inner ->
            when (inner) {
                COLOR, BACK -> initColorInstruments(view, isBack = newState == BACK)
                FORMAT -> initFormatInstruments()
                FONT -> initFontInstruments(view)
                SIZE -> initSizeInstruments(view)
                VOLUME, TRIM -> initVideoEditModel(view)
                SHAPE -> initShapesModel(view)
                MOVE -> initMoveModel(view)
                SLIDE -> initSlidesModel(view)
                EDIT_MUSIC -> {}
                else -> {
                }
            }
        } ?: modelHolder.onAdditionalPanelClosed()
    }

    private fun showCorrectInstruments(state: InstrumentMain) {
        this.currentInstrumentMain = state
    }

    fun closeInnerPanel() {
        updateInnerInstrument(null)
        modelHolder.removeAdditionalModel()
    }

    fun pickImage() {
        closeInnerPanel()
        fullScreenTools.value = FullScreenTools.PICK_IMAGE
    }
}
