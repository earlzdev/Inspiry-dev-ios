//
//  MainScreenViewModelApple.swift
//  iosApp
//
//  Created by rst10h on 28.12.21.
//

import Foundation
import shared
import SwiftUI

class MainScreenViewModelApple: ViewModelBridge<MainScreenViewModel> {
    
    @Published
    var isLoaded = false
    
    private static let emptyCategory = TemplateCategory(id: "stories", displayName: MR.strings().tab_templates, templatePaths: [], icon: TemplateCategoryIcon.none)
    
    private let templateReadWrite: TemplateReadWrite = Dependencies.resolveAuto()
    
    @Published
    var fullScreenSelection: String?
    
    @Published
    private(set) public var currentPage: MainScreenPages?
    
    @Published
    private(set) public var currentCategoryIndex: Int
    
    @Published
    private(set) public var bannerVisible: Bool
    
    @Published
    public var feedbackDialogVisible: Bool = false
    
    @Published
    public var templateActionsDialogVisible: Bool = false
    
    public let helper: TemplatesAdapterHelper
    
    @Published
    var templateCategories: [TemplateCategory] = []
       
    var myStoriesViewModel = MyTemplatesViewModelApple()
       
    var templateModelsCache: [TemplatePath: InspTemplateViewApple] = [TemplatePath: InspTemplateViewApple]()
       
    private(set) public var selectedForAction: TemplatePath? = nil
    
    private(set) public var selectedTemplateName: String = ""
    

    let mainTemplates: [TemplateCategory]
    
    override init(_ coreModel: MainScreenViewModel) {
        
        currentPage = coreModel.currentPage.value as? MainScreenPages
        currentCategoryIndex = coreModel.currentCategoryIndex.value as! Int
        bannerVisible = coreModel.bannerVisible.value as! Bool
      
        mainTemplates = coreModel.categoryProvider.getTemplateCategories(isPremium: false)
        
        let categories = coreModel.getCategories()
        templateCategories = categories
        let paths = TemplateReadWrite.companion.predefinedTemplatePaths(categories: categories)
        self.helper = TemplatesAdapterHelper(coroutineContext: CoroutinesUtilKt.ioDispatcherCommon,templates: paths, myStories: false, hasPremium: false, instagramSubscribed: false, categories: categories)
        
        super.init (coreModel)
        
        self.fullScreenSelection = onBoardingWasShown() && !Constants.ONBOARDING_ALWAYS_VISIBLE ? nil : Constants.TAG_ONBOARDING_VIEW
        
        CoroutineUtil.watch(state: coreModel.currentPage, onValueReceived: { [weak self] in
            self?.currentPage = $0
        })
        CoroutineUtil.watch(state: coreModel.currentCategoryIndex, onValueReceived: { [weak self] in self?.currentCategoryIndex = $0 })
        CoroutineUtil.watch(state: coreModel.bannerVisible, onValueReceived: { [weak self] in self?.bannerVisible = $0 })
                      
        print("init viewmodel main")
        
        loadTemplates(.templates)
    }
    
    func selectNewPage(newPage: MainScreenPages) {
        if (newPage == currentPage) { return }
        
        if (newPage == .story) {
            myStoriesViewModel.loadMyStories() {
                
            }
        }
        withAnimation {
            currentPage = nil
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.5) {
                self.coreModel.selectPage(newPage: newPage)
                if (newPage != .story) {
                    self.resetHelper()
                } else {
                    self.templateCategories = [self.emptyCategory()]
                }
            }
        }
    }
    
    func clearMyStoriesCache() {
        
    }
    
    func topTabsItems() -> [MenuItem] {
        var menu = [
            MenuItem(iconSize: 20, text: MR.strings().tab_templates.localized(), icon: "ic_templates_enabled", inactiveIcon: "ic_templates_disabled"),
            MenuItem(iconSize: 20, text: MR.strings().tab_my_stories.localized(), icon:  "ic_stories_enabled", inactiveIcon: "ic_stories_disabled")]
        
        if (coreModel.licenseManager.hasPremiumState.value as? Bool != true) {
            menu.append(MenuItem(iconSize: 20, text: MR.strings().tab_pro.localized(), icon: "ic_pro_enabled", inactiveIcon: "ic_pro_disabled"))
        }
        
        return menu
        
    }
    
    func setMainTab(tab: MainScreenPages) {
        currentPage = tab
    }
    
    func resetHelper() {
        helper.categories = coreModel.getCategories()
        helper.templates =  TemplateReadWrite.companion.predefinedTemplatePaths(categories: helper.categories!)
        templateCategories = helper.categories ?? []
    }
    
    var templatesLoaded = MainScreenPages.test
    
    func loadTemplates(_ page: MainScreenPages) {
        print("load templates page = \(page) loaded \(page == templatesLoaded)")
        if (page == templatesLoaded) { return }
        resetHelper()
        
        helper.loadTemplatesApple(templatesReadWrite: templateReadWrite, onLoad: { [self] (index, template, path) in
            DispatchQueue.main.sync {
                if let tv =  templateModelsCache[path] {
                    print("load template set loaded")
                    //tv.setTemplate(template: template)
                } else {
                    let tv = InspTemplateViewApple(templatePath: path)
                    //tv.logLevel = 1
                    //tv.debugName = path.path.getFileName()
                    tv.setTemplate(template: template)
                    templateModelsCache[path] = tv
                }
                
                if (index.intValue >= (helper.templatesCount().int - 1) && !isLoaded) {
                    if (currentPage == .story) {
                        templatesLoaded = .test //todo wrong code
                    } else {
                        templatesLoaded = currentPage ?? .templates //wtf?
                        myStoriesViewModel.loadMyStories() {[weak self] in
                            withAnimation {
                                self?.isLoaded = true
                            }
                        }
                    }
                }
            }
            
        })
    }
    
    func saveTemplate(template: Template, templatePath: TemplatePath, complectionHasndler: @escaping () -> Void) {
        DispatchQueue.main.async {
            self.templateReadWrite.saveTemplateToFile(template: template, existingPath: templatePath, currentTime: Int64(Date().timeIntervalSinceReferenceDate))
            complectionHasndler()
        }
    }
    
    func openMyTemplateAction(_ path: TemplatePath, name: String) {
        selectedForAction = path
        selectedTemplateName = name
        templateActionsDialogVisible.toggle()
    }
    
    func updateTemplateName(_ name: String) {
        selectedTemplateName = name
    }
    
    func getCachedTemplateModel(templatePath: TemplatePath) -> InspTemplateViewApple {

        if let templateModel = templateModelsCache[templatePath] {
            return templateModel
        } else {
            fatalError("unable to get template from cache \(templatePath.path)")
        }
    }
    
    func myTemplateAction(_ action: MyTemplatesActions) {
        
        
        guard let templatePath = selectedForAction else { return }
        guard let template = myStoriesViewModel.templatesCache[templatePath]?.template_ else { return }
        
        templateActionsDialogVisible.toggle()
        
        if (action == .remove) {
            myStoriesViewModel.removeStory(path: templatePath) { [weak self] in
                self?.selectedForAction = nil
            }
        }
        if (action == .copy) {
            myStoriesViewModel.copyStory(path: templatePath) { [weak self] in
                
            }
        }
        
        if (action == .rename) {
            template.name = selectedTemplateName
            saveTemplate(template: template, templatePath: templatePath) { [weak self] in
                if (self?.selectedForAction) != nil {
                    print("reload template \(templatePath.path)")
                    self?.myStoriesViewModel.unloadTemplate(path: templatePath)
                    self?.myStoriesViewModel.loadMyStories() {}
                }
                self?.objectWillChange.send()
            }
        }
    }
       
    func emptyCategory() -> TemplateCategory {
        return MainScreenViewModelApple.emptyCategory
    }
    
    func getPaths(categoryIndex: Int) -> [TemplatePath] {
        return helper.getPathListForCategory(categoryIndex: Int32(categoryIndex))
    }
    
    func getTemplateFromCache(templatePath: TemplatePath) -> Template? {
        return helper.getTemplateFromCacheOrNull(templatePath: templatePath)
    }
    
    func onDisappearPage(_ page: MainScreenPages, scrollBehavior: LinkedScrollBehavior) {
        if (currentPage != page) {
            scrollBehavior.parentScrollChanged(newScrollPosition: 0)
        } else {
            helper.stopLoading()
        }
    }
    
    func onBoardingWasShown() -> Bool {
        let settings: Settings = Dependencies.resolveAuto()
        let onBoardingWasShown = settings.getBoolean(key: Constants.KEY_ONBOARDING_SHOWN, defaultValue: false)
        if (onBoardingWasShown) { return true }
        settings.putBoolean(key: Constants.KEY_ONBOARDING_SHOWN, value: true)
        return false
    }
    
    static func Create() -> MainScreenViewModelApple {
        return MainScreenViewModelApple(MainScreenViewModel(licenseManager: Dependencies.resolveAuto(), categoryProvider: Dependencies.resolveAuto()))
    }
}

enum MyTemplatesActions {
    case remove, copy, rename
}
