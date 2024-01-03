//
//  EditViewModelApple.swift
//  iosApp
//
//  Created by vlad on 12/1/22.
//

import Foundation
import SwiftUI
import shared
import PhotosUI

class EditViewModelApple: PickMediaReciver, ObservableObject {
    
    @Published
    var mediaResult: [PickMediaResult] = []
    
    @Published
    var removeBGLoading: Bool = false
    
    @Published
    var waitLoading = false {
        didSet {
            if (!mediaResult.isEmpty && !waitLoading) {
                coreModel?.onImagePicked(result: mediaResult) { [self] in
                    templateView.setFrameForEdit()
                }
                loadingProgress = nil
            }
            mediaResult.removeAll()
        }
    }
    @Published
    var loadingProgress: Double? = nil
    
    @Published
    var saveConfirmationDialog: Bool = false
    
    var wrapperHelper: EditWrapperHelperApple
    
    @Published
    var galleryIsShown: Bool = false
    
    
    var templateView: InspTemplateViewApple
    
    
    @Published
    var editMainState: EditMainState = .Edit
    
    @Published
    var isInitialized: Bool = false {
        didSet {
            print("initialized \(isInitialized)")
        }
    }
    @Published
    var isDarkTheme: Bool = false
    
    @Published
    var keyboardIsShown: Bool = false
    
    @Published
    var fullScreenSelection: String? = nil {
        didSet {
            isDarkTheme = fullScreenSelection != nil
            instrumentsManager.resetFullScreenTool()
        }
    }
    
    @Published
    var notAuthorizedDialog = false
    
    @Published
    var displayDone: Bool = false
    
    @Published var instrumentState: EditInstrumentsState {
        didSet {
            self.objectWillChange.send()
        }
    }
    
    
    @Published
    var isCloseablePanelOpened: Bool = false
    
    @Published
    var formatState: TemplateFormat = .story
    
    @Published
    var showProWatermark: Bool = true
    
    var instrumentsManager: InstrumentsManager { return coreModel!.instrumentsManager }
    
    var templateViewModel: TemplateViewModel? = nil
    
    var coreModel: EditViewModel? = nil
    
    let analyticsManager: AnalyticsManager
    let settings: Settings
    
    init(templatePath: TemplatePath, initialOriginalData: OriginalTemplateData?) {
        
        print("create edit model \(templatePath.path)")
        
        templateView = InspTemplateViewApple(templatePath: templatePath)
        templateView.templateMode = .edit
        templateView.isMusicEnabled = true
        
        self.wrapperHelper = EditWrapperHelperApple(helper: EditWrapperHelper(
            scope: CoroutinesUtilKt.createDefaultScope(),
            templateView: templateView,
            dpFactor: 1.cg.float,
            externalResourceDao: Dependencies.resolveAuto()
        ))
        self.analyticsManager = Dependencies.resolveAuto()
        self.settings = Dependencies.resolveAuto()
        
        self.templateViewModel = TemplateViewModel(templateReadWrite: Dependencies.resolveAuto())
        let editModel = EditViewModel(
            licenseManger: Dependencies.resolveAuto(),
            templateCategoryProvider: Dependencies.resolveAuto(),
            templateViewModel: templateViewModel!,
            freeWeeklyTemplatesNotificationManager: Self.freeWeekly(),
            scope: CoroutinesUtilKt.createDefaultScope(),
            templateView: templateView,
            appViewModel: Dependencies.resolveAuto(),
            storyUnfinishedNotificationManager: Self.storyUnfinished(),
            templateSaver: Dependencies.resolveAuto(),
            mediaReadWrite: Dependencies.resolveAuto(),
            templatePath: templatePath,
            initialOriginalTemplateData: initialOriginalData,
            externalResourceDao: Dependencies.resolveAuto(),
            settings: self.settings,
            remoteConfig: Dependencies.resolveAuto(),
            analyticsManager: analyticsManager,
            platformFontPathProvider: Dependencies.resolveAuto(),
            uploadedFontsProvider: Dependencies.resolveAuto(),
            textCaseHelper: Dependencies.resolveAuto())
        
        self.coreModel = editModel
        
        
        self.instrumentState = editModel.instrumentsManager.instrumentsState.value as! EditInstrumentsState
        
        let proState = editModel.getTopBarActionDisplayFlow()
        
        editModel.displayWatermarkCollector { [weak self] value in
            DispatchQueue.main.async {
                self?.showProWatermark = value.boolValue
                print("watermark enabled = \(value.boolValue)")
            }
        }
        
        CoroutineUtil.watch(state: instrumentsManager.instrumentsState, onValueReceived: {
            [weak self] in self?.instrumentState = $0
        })
        
        CoroutineUtil.watch(state: instrumentsManager.fullScreenTools, onValueReceived: {
            [weak self] in
            self?.updateFullScreenTool(fullScreenItem: $0)
        }
        )
        
        CoroutineUtil.watch(state: templateView.isInitialized, onValueReceived: {
            [weak self] in self?.isInitialized = $0
        })
        
        CoroutineUtil.watch(state: instrumentsManager.isCloseablePanelOpened, onValueReceived: {
            [weak self] in self?.isCloseablePanelOpened = $0
        })
        
        CoroutineUtil.watch(state: coreModel!.templateFormatState, allowNilState: false, onValueReceived: {
            [weak self] in self?.formatState = $0
        })
        
        CoroutineUtil.watch(state: coreModel!.isKeyboardShown, onValueReceived:  {
            [weak self] in self?.keyboardIsShown = $0
        })
        
        editModel.templateInitializedCallbacks()
        
        //        templateView.loadTemplate()
        //        templateView.template_.originalData = initialOriginalData
        
        templateView.doWhenTemplateInitialized { [weak self] in
            if (templatePath is PredefinedTemplatePath) {
                if let templateView = self?.templateView {
                    self?.analyticsManager.templateClick(template: templateView.template_, isStatic: templateView.isStatic())
                }
            }
        }
        
        coreModel?.loadTemplatePath()
        
    }
    private var _innerModelHelper: InstrumentsModelHelperApple? = nil
    
    func getInnerModelHelper() -> InstrumentsModelHelperApple {
        if (_innerModelHelper == nil) {
            _innerModelHelper = InstrumentsModelHelperApple(instrumentsManager: instrumentsManager) { self.notifyTemplateChanged() }
        }
        
        return _innerModelHelper!
        
    }
    
    private var exportViewModel: ExportViewModel? = nil
    
    func getExportViewModel() -> ExportViewModel {
        let model = ExportViewModel(editableTemplate: templateView)
        self.exportViewModel = model
        return model
    }
    
    private func showGallery() {
        PHPhotoLibrary.requestAuthorization(for: .readWrite) { status in
            switch status {
            case .authorized:
                DispatchQueue.main.async { [weak self] in
                    self?.galleryIsShown = true
                }
            @unknown default:
                DispatchQueue.main.async { [weak self] in
                    let showNotAuthorized = self?.settings.getBoolean(key: Constants.media_library_second_access_dialog, defaultValue: false) ?? false
                    self?.notAuthorizedDialog = showNotAuthorized
                    if (!showNotAuthorized) {
                        self?.settings.putBoolean(key: Constants.media_library_second_access_dialog, value: true)
                    }

                }
            }
        }
        
    }
    
    func updateFullScreenTool(fullScreenItem: FullScreenTools?) {
        print("fullscreentool \(fullScreenItem)")
        DispatchQueue.main.async { [self] in
            if (fullScreenItem == .pickImage) {
                let selected = templateView.selectedView
                coreModel?.pickMediaConfigure(view: selected, isReplace: false)
                templateView.selectedView = nil
                showGallery()
                instrumentsManager.resetFullScreenTool()
                return
            }
            if (fullScreenItem == .removeBg) {
                instrumentsManager.resetFullScreenTool()
                removeBGLoading = true
                let view = coreModel?.mediasToRemoveBg?.first
                if (view != nil) {
                    coreModel?.mediasToRemoveBg = nil
                }
                if let media = view ?? templateView.selectedView?.asInspMediaView() {
                    RemoveBgViewModelApple(imagePaths:  [media.media.originalSource!]) { result, error in
                        if self.removeBGLoading == true {
                            self.removeBGLoading = false
                            if (result?.count == 1) {
                                if let first = result?.first {
                                    self.coreModel?.insertRemovedBgView(mediaView: media, resultItem: first)
                                }
                            } else {
                            }
                        }
                    }
                    
                    
                } else {
                    print("image not selected")
                }
                
            }
            if (fullScreenItem == .replace) {
                let selected = templateView.selectedView
                coreModel?.pickMediaConfigure(view: selected, isReplace: true)
                showGallery()
                instrumentsManager.resetFullScreenTool()
                return
            }
            if (fullScreenItem == .pickSingleMedia) {
                templateView.selectedView = nil
                coreModel?.pickMediaConfigure(view: nil, isReplace: false)
                showGallery()
                instrumentsManager.resetFullScreenTool()
            }
            
            fullScreenSelection = Self.getFullscreenSelectionString(fullScreenItem: fullScreenItem) ?? fullScreenSelection
        }
    }
    
    func getFontForEdit() -> UIFont {
        guard let tv = templateView.selectedView?.asInspTextView() else {
            if (Dependencies.isDebug()) {
                fatalError("InspTextView not found for text edit")
            }
            else {
                return UIFont.systemFont(ofSize: 40)
            }
        }
        
        let fontObtainer: PlatformFontObtainerImpl = Dependencies.diContainer.resolveAuto()
        
        guard let font: UIFont = try? fontObtainer.getTypefaceFromFontData(fontData: tv.media.font) else {
            return  UIFont.systemFont(ofSize: 40)
        }
        
        return font.withSize(40)
    }
    
    func getTextForEdit() -> String {
        guard let tv = templateView.selectedView?.asInspTextView() else {
            if (Dependencies.isDebug()) {
                fatalError("InspTextView not found for text edit")
            }
            else {
                return "unknown text"
            }
        }
        
        return tv.media.text
    }
    
    func onTextChanged(newText: String) {
        
        guard let tv = templateView.selectedView?.asInspTextView() else {
            if (Dependencies.isDebug()) {
                fatalError("InspTextView not found for text edit")
            }
            else {
                return
            }
        }
        
        coreModel?.editTextDone(inspView: tv, newText: newText)
    }
    
    static func getFullscreenSelectionString(fullScreenItem: FullScreenTools?) -> String? {
        switch fullScreenItem {
        case FullScreenTools.pickImage: return Constants.TAG_PICK_IMAGE
        case FullScreenTools.music: return Constants.TAG_MUSIC_LIBRARY
        case FullScreenTools.textAnim: return Constants.TAG_ANIMATION_VIEW
        case FullScreenTools.stickers: return Constants.TAG_STICKERS_VIEW
        case FullScreenTools.subscribe: return Constants.TAG_SUBSCRIBE_VIEW
        default:
            return nil
        }
    }
    
    static func freeWeekly() -> FreeWeeklyTemplatesNotificationManager {
        return FreeWeeklyTemplatesNotificationManager(
            settings: Dependencies.resolveAuto(),
            notificationScheduler: Dependencies.resolveAuto(),
            licenseManager: Dependencies.resolveAuto(),
            scope: CoroutinesUtilKt.createDefaultScope(),
            loggerGetter: Dependencies.resolveAuto(),
            remoteConfig: Dependencies.resolveAuto(),
            freeTemplatesPeriodDays: SharedConstants.shared.FreeTemplatesPeriodDays)
    }
    
    static func storyUnfinished() -> StoryUnfinishedNotificationManager {
        return StoryUnfinishedNotificationManager(
            settings: Dependencies.resolveAuto(),
            config: Dependencies.resolveAuto(),
            notificationScheduler: Dependencies.resolveAuto(),
            json: Dependencies.resolveAuto(),
            loggerGetter: Dependencies.resolveAuto(),
            isDebug: DebugManager().isDebug)
    }
    
    func onClickDone() {
        instrumentsManager.mayRemoveAdditionalPanel()
    }
    
    func onClickPreview() {
        withAnimation(.easeOut) {
            editMainState = .Preview
        }
        templateView.isMusicEnabled = true
        templateView.setFrame(frame: 0)

        coreModel?.toPreviewMode()
    }
    
    func onClickExport() {
        withAnimation(.easeOut) {
            editMainState = .Export
        }
    }
    
    func onClickBack(fallback: @escaping (Bool) -> ()) {
        
        if fullScreenSelection != nil {
            fullScreenSelection = nil
        } else if editMainState != EditMainState.Edit {
            if (editMainState == .Export) {
                exportViewModel?.onClickBack()
                exportViewModel = nil
                RenderSettings.clearCache()
            }
            withAnimation(.easeOut) {
                editMainState = .Edit
            }
            
            if (templateView.templateMode != .edit) {
                coreModel?.toEditMode()
            }
            
        } else {
            if (coreModel?.backAction() == true) {
                if (coreModel?.showConfirmationIsNeed(confirmed: false, onSaveFinished: {
                    saved in
                    fallback(saved.boolValue)
                    self.templateView.removeViews()
                }) == true) {
                    withAnimation {
                        saveConfirmationDialog = true
                    }
                } else {
                    //fallback(false) //why?
                }
            }
        }
    }
    
    func saveAndExit() {
        coreModel?.saveTemplateToFile() {
            self.templateView.removeViews()
        }
    }
    
    func backToEdit() {
        DispatchQueue.main.async {
            self.fullScreenSelection = nil
        }
        
    }
    private var stickersModel: StickersViewModelApple? = nil
    func getStickersModel() -> StickersViewModelApple {
        if let model = stickersModel {
            return model
        } else {
            stickersModel = StickersViewModelApple.SharedInstance()
            return stickersModel!
        }
    }
    
    private var textAnimModel: TextAnimViewModelApple? = nil // single instance of TextAnimViewModelApple
    func getTextAnimModel() -> TextAnimViewModelApple {
        if let model = textAnimModel {
            model.coreModel.currentText = templateView.selectedView?.asInspTextView()?.textView?.text
            return model
        } else {
            textAnimModel = TextAnimViewModelApple(text: templateView.selectedView?.asInspTextView()?.textView?.text)
            return textAnimModel!
        }
    }
    
    func notifyTemplateChanged() {
        templateView.objectWillChange.send()
    }
}
