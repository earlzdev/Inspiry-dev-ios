//
//  TextAnimViewModelApple.swift
//  iosApp
//
//  Created by rst10h on 9.09.22.
//

import Foundation
import shared

class TextAnimViewModelApple: ObservableObject {
    let coreModel: TextAnimViewModel
    
    @Published
    var categories: [String]? = nil
    
    @Published
    var fullScreenSelection: String? = nil
    
    @Published
    var currentCategory: String? = nil
    
    @Published
    var currentAnimations: [MediaWithRes]? = nil
    
    @Published
    var selectedAnimation: MediaWithRes? = nil {
        didSet {
            self.updateTopPreview(anim: selectedAnimation)
        }
    }
    
    @Published
    var topPreview: InspTemplateViewApple? = nil
    
    @Published
    var hasPremium: Bool
    
    let tabs: [String]
    
    var templatesWithAnims: [MediaWithRes: InspTemplateViewApple] = [:]
    
    init(text: String? = nil) {
        print("create text anim model")
        let errorHandler = ErrorHandlerImpl(toastManager: Dependencies.resolveAuto())
        let provider: TextAnimProvider = Dependencies.resolveAuto()
        let licenseManager: LicenseManager = Dependencies.resolveAuto()
        self.hasPremium = (licenseManager.hasPremiumState.value as! KotlinBoolean).boolValue
        coreModel = TextAnimViewModel(
            currentText: text,
            textCaseHelper: Dependencies.resolveAuto(),
            json: Dependencies.resolveAuto(),
            unitsConverter: Dependencies.resolveAuto(),
            provider: provider,
            analyticsManager: Dependencies.resolveAuto(),
            errorHandler: errorHandler,
            loggerGetter: Dependencies.resolveAuto(),
            initialTabNum: 0,
            initialAnimationPath: nil)
        tabs = coreModel.getTextAnimationTabs() as! [String]
        self.categories = provider.getCategories()
        
        CoroutineUtil.watch(state: licenseManager.hasPremiumState,  allowNilState: false, onValueReceived: {[weak self] in
            self?.hasPremium = $0
        })
        
        CoroutineUtil.onReceived(state: coreModel.currentAnimations, onValueReceived: {[weak self] in
            self?.updateAnimations()
        })
        
        CoroutineUtil.watch(state: coreModel.currentTabNum, onValueReceived: {[weak self] in
            self?.updateCategory(id: $0)
        })
        
        CoroutineUtil.watch(state: coreModel.currentPreviewAnimation, onValueReceived: { [weak self] in
            self?.selectedAnimation = $0
        })
        
    }
    
    func onDisappear() {
        templatesWithAnims.removeAll()
        topPreview = nil
    }
    
    private func updateCategory(id: Int) {
        self.currentCategory = tabs[id]
    }
    
    private func updateTopPreview(anim: MediaWithRes?) {
        if let anim = anim {
            self.topPreview?.stopPlaying()
            let lastSize = topPreview?.getTemplateSize()
            let templateView = InspTemplateViewApple(templatePath: nil, template: nil)
            templateView.setLogLevel(value: 0)
            templateView.debugName = anim.res.path.getFileName()
            self.topPreview = templateView
            if (lastSize != nil) {
                templateView.onTemplateSizeChanged(newSize: lastSize!)
            }
            self.topPreview?.templateVisible = true
            self.coreModel.doInitPreviewTemplateView(templateView: templateView)
            self.coreModel.previewAnimation(media: anim.media, templateView: templateView)
        }
        else {
            self.topPreview = nil
        }
    }
    
    private func updateAnimations() {
        if let anims = coreModel.currentAnimations.value as! [MediaWithRes]? {
            var position = 0
            for anim in anims {
                if (templatesWithAnims[anim] == nil) {
                    let templateView = InspTemplateViewApple(templatePath: nil, template: nil)
                    self.coreModel.doInitPreviewTemplateView(templateView: templateView)
                    self.coreModel.previewAnimationInList(media: anim.media, position: Int32(position), templateView: templateView)
                    position += 1
                    //templateView.shouldHaveBackground = false
                    //templateView.loadTemplate(fromPath: false)
                    templatesWithAnims[anim] = templateView
                }
            }
            currentAnimations = anims
        }
    }
    
    func onSaveClick() -> Bool {
        guard let selected = selectedAnimation,
              let tv = templatesWithAnims[selected] else { return false}
        if (coreModel.shouldOpenSubscribeOnClickSave(hasPremium: hasPremium, templateView: tv)) {
            DispatchQueue.main.async {
                self.fullScreenSelection = Constants.TAG_SUBSCRIBE_VIEW
            }
            return false
        } else {
            coreModel.onClickSaveTemplate()
            return true
        }
    }
    
    deinit {
        print("destroy text anim model")
    }
}
