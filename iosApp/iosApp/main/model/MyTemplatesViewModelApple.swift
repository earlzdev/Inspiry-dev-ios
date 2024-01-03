//
//  MyTemplatesViewModelApple.swift
//  iosApp
//
//  Created by rst10h on 10.01.22.
//

import Foundation
import shared

class MyTemplatesViewModelApple: ViewModelBridge<MyTemplatesViewModel> {
    private let templateReadWrite: TemplateReadWrite = Dependencies.resolveAuto()
    
    public let helper: TemplatesAdapterHelper
    
    @Published
    var loadedTemplates: [TemplatePath]
    
    @Published
    var templatesCache: [TemplatePath:InspTemplateViewApple] = [:]
    
    init() {
        
        print ("create my templates model")
        
        loadedTemplates = []
        
        let coreModel = MyTemplatesViewModel(templateReadWrite: templateReadWrite)
        
        self.helper = TemplatesAdapterHelper(templates: [], myStories: true, hasPremium: false, instagramSubscribed: false, categories: nil)
        
        super.init(coreModel)
        
        CoroutineUtil.watch(state: coreModel.templates, allowNilState: false, onValueReceived: {
            [weak self] in self?.onLoadedTemplatesChanged(paths: $0)
        })
        
    }
    
    func loadMyStories(finishHandler: @escaping () -> Void) {
        coreModel.loadMyStories() {
            finishHandler()
        }
    }
    
    func onLoadedTemplatesChanged(paths: [TemplatePath]) {
        guard !paths.isEmpty else { return }
        
        helper.clear()
        helper.setLoadableTemplates(templatePaths: paths)
        helper.loadTemplatesApple(templatesReadWrite: Dependencies.resolveAuto()) {[weak self] index, template, path in
            
            func checkIfLast() {
                if (index.intValue >= paths.count - 1) {
                    DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                        self?.loadedTemplates = paths
                    }
                }
            }
            
            
            if let _ = self?.templatesCache[path] {
                checkIfLast()
            } else {
                DispatchQueue.main.async {
                    let tv = InspTemplateViewApple(templatePath: path)
                    tv.templateMode = .preview
                    tv.setTemplate(template: template, isMyStories: true)
                    self?.templatesCache[path] = tv
                    checkIfLast()
                }
            }
        }
    }
    
    func onDisappear() {
        print("deinit loaded muystories")
        templatesCache.values.forEach { tv in
            tv.unloadTemplate()
        }
        templatesCache.removeAll()
    }
    func removeStory(path: TemplatePath, complectionHandler: @escaping () -> Void) {
        
        guard let template = templatesCache[path]?.template_ else { return }
        
        templateReadWrite.deleteTemplateFiles(template: template, path: path, externalResourceDao: Dependencies.resolveAuto())
        let templateView = templatesCache[path]
        templateView?.unloadTemplate()
        templateView?.templateVisible = false
        helper.removeTemplate(path: path) { [self] in
            templatesCache.removeValue(forKey: path)
            complectionHandler()
        }
    }
    
    func copyStory(path: TemplatePath, complectionHandler: @escaping () -> Void) {
        
        guard let template = templatesCache[path]?.template_ else { return }
        print("stories template copy")
        DispatchQueue.main.async { [self] in
            self.helper.doCopyTemplate(
                templateReadWrite: templateReadWrite,
                template: template,
                templatePath: path,
                onCopied: { [weak self] (t, p) in
                    print("stories template copy finished")
                    self?.loadMyStories() {
                        complectionHandler()
                    }
                }
            )
        }
    }
    func reloadTemplate(path: TemplatePath) {
        unloadTemplate(path: path)
        print("reload template (remove) \(path.path)")
        onLoadedTemplatesChanged(paths: loadedTemplates)
    }
    
    func unloadTemplate(path: TemplatePath) {
        guard let tv = templatesCache[path] else { return }
        tv.unloadTemplate()
        tv.templateVisible = false
        templatesCache.removeValue(forKey: path)
    }
}
