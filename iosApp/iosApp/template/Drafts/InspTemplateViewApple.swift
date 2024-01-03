//
//  InspTemplateViewApple.swift
//  iosApp
//
//  Created by rst10h on 27.01.22.
//

import Foundation
import shared
import UIKit
import SwiftUI

class InspTemplateViewApple: InspTemplateView, ObservableObject {
    let templatePath: TemplatePath?
    
    var logLevel = 0
    var debugName: String? = nil
    @Published
    var isLoaded = false
    
    @Published
    var forPremium = false
    
    var debugFrames = true
    
    var templateSizeAvailable: Bool = false
    var templateVisible: Bool = false {
        didSet {
            onVisibleStatusChanged(templateVisible)
        }}
    
    let _viewScope = CoroutinesUtilKt.createDefaultScope()
    let _containerScope = CoroutinesUtilKt.createScope()
    
    override var viewScope: Kotlinx_coroutines_coreCoroutineScope? {
        get {
            //fatalError("get view scope")
            return _viewScope
        }
    }
    
    override var containerScope: Kotlinx_coroutines_coreCoroutineScope {
        get {
            //fatalError("get container scope")
            return _containerScope
        }
    }
    
    private var template: Template? = nil
    
    private var maskDictionary: Dictionary<String, String> = [:]
    
    init(templatePath: TemplatePath? = nil, template: Template? = nil) {
        self.templatePath = templatePath
        self.template = template
        
        let infoVM = InfoViewModelImpl(coroutineScope: CoroutinesUtilKt.createDefaultScope())
        let guideLineManager = GuideLineManagerApple()
        
        //let viewsFactory = EmptyViewFromMediaFactory(viewProvider: Dependencies.resolveAuto())
        
        super.init(
            loggerGetter: Dependencies.resolveAuto(),
            unitsConverter: Dependencies.resolveAuto(),
            infoViewModel: infoVM,
            json: Dependencies.resolveAuto(),
            textCaseHelper: Dependencies.resolveAuto(),
            fontsManager: Dependencies.resolveAuto(),
            templateSaver: Dependencies.diContainer.resolve(TemplateReadWrite.self)!,
            guidelineManager: guideLineManager,
            viewsFactory: Dependencies.resolveAuto(),
            movableTouchHelperFactory: MovableTouchHelperFactoryApple(),
            fileSystem: OkioFileSystem.companion.SYSTEM,
            initialTemplateMode: TemplateMode.listDemo
        )
        
        CoroutineUtil.watch(state: super.isInitialized, onValueReceived: {
            [weak self] in self?.initialized(b: $0)
        })
    }
    
    var onInitializedAction: (() -> Void)? = nil
    
    
    func initialized(b: Bool) {
        if (logLevel >= 1) {
            print("template is initialized \(b) \(debugName)")
        }
        if b {
            if (isMyStories) {
                self.setFrameForEdit()
                objectWillChanged()
            } else {
                if (templateMode == TemplateMode.listDemo) {
                    onFrameUpdated(value: maxFrames - 1)
                } else {
                    onFrameUpdated(value: 0)
                }
            }
            //self.objectWillChange.send()
            onInitializedAction?()
        }
    }
    
    func onVisibleStatusChanged(_ visible: Bool) {
        //todo: remove objects when template invisible in list
        if (logLevel >= 1) {
            print("template is visible now \(visible)")
        }
        if (visible && (isInitialized.value as! KotlinBoolean).boolValue) {
            if (isMyStories) {
                self.setFrameForEdit()
            }
            objectWillChanged()
        }
//        if (!visible) {
//            unloadTemplate()
//            isLoaded = false

//        } else {
//            if (!isLoaded) {
//                loadTemplate()
//            }
//        }
    }
    
    override func removeViews() {
        allViews.forEach{ view in
            let v = (view as! InspView<AnyObject>)
            (v.media as! Media).view = nil
            (v.animationHelper as? AnimationHelperApple)?.inspView = nil
            v.releaseInner()
            if (v.asInspGroupView() != nil) {
                print("aptempt to remove group \((v.media as! Media).id)")
            }
        }
        
        super.removeViews()
    }
    
    var isMusicEnabled: Bool = false
    override func startPlaying(resetFrame: Bool, mayPlayMusic: Bool) {
        if (logLevel >= 1) {
            print("start playing \(templatePath?.path ?? debugName) visible \(isVisible()) initialized \((isInitialized.value as! KotlinBoolean).boolValue)")
        }
        super.startPlaying(resetFrame: resetFrame, mayPlayMusic: isMusicEnabled)
    }
    
    private let templateReadWrite: TemplateReadWrite = Dependencies.resolveAuto()
    
    func templateFormat() -> TemplateFormat {
        return template?.format ?? TemplateFormat.story
    }
    
    func prepareAnimations() {
        
    }
    override func unpackTextures(template: Template) {
        print("unpack textures for template \(debugName)")
        TextureMaskHelper().maskProcessing(template: template, json: json) { id, maskId, index in
            if (index == 0) {
                self.maskDictionary[id] = maskId
            }
        }
    }
    
    func loadTemplate(fromPath: Bool = false) {
        if (isLoaded) { return }
        if (logLevel >= 1) {
            print("load template! \(debugName)")
        }
        if fromPath { template = nil }
        
        if template == nil {
            if let templatePath = templatePath {
                template = templateReadWrite.loadTemplateFromPath(path: templatePath)
            }
        }
        
        
        
        super.loadTemplate(template: template!)
        
        forPremium = template?.availability == TemplateAvailability.premium
        isLoaded = true
    }
    
    var isMyStories = false
    
    func setTemplate(template: Template, isMyStories: Bool = false) {
        isLoaded = false
        self.isMyStories = isMyStories
        self.template = template
        loadTemplate(fromPath: false)
    }
    
    func getTemplatePalette() -> TemplatePalette {
        if ((isInitialized.value as? KotlinBoolean) == true) {
            return template_.palette
        }
        else {
            return TemplatePalette.companion.getEmpty()
        }
    }
    
    var templateWidth: CGFloat = 0
    var templateHeight: CGFloat = 0
    
    func getChildren(inspParent: InspParent?) -> [InspView<AnyObject>] {
        let res = inspParent?.inspChildren.filter {
            $0.media.textureIndex??.intValue == nil
        }
        return res ?? []
    }
    
    func onTemplateSizeChanged(newSize: CGSize) {
        if (newSize.width == templateWidth && newSize.height == templateHeight) {
            return
        }
        if (newSize.width == 0 || newSize.height == 0) {
            templateSizeAvailable = false
            return
        }
        if (logLevel >= 1) {
            print("template size changed new size: \(newSize)")
        }
        templateWidth = newSize.width
        templateHeight = newSize.height
        DispatchQueue.global().async {
            self.updateChildrenSize(parentSize: newSize)
            self.templateSizeAvailable = true
            self.currentSize.setValue(Size(width: self.templateWidth.toInt32, height: self.templateHeight.toInt32))
            DispatchQueue.main.async {
                self.objectWillChange.send()
            }
        }
    }
    
    func getTemplateSize() -> CGSize {
        return CGSize(width: templateWidth, height: templateHeight)
    }
    
    func getMaskView(viewId: String) -> InspView<AnyObject>? {
        let viewa = allViews.first(where: {($0 as! InspView<AnyObject>).media.id == maskDictionary[viewId]}) as? InspView<AnyObject>
        return viewa
    }
    
    /**
     it is not right!!
     need to refactor it!
     */
    func onParentSizeChangedRecursive(parent: InspParent, newSize: CGSize) {
        let templateSize = CGSize(width: viewWidth.cg, height: viewHeight.cg)
        if let parent = parent as? InspGroupView {
            (parent.view as? ViewPlatformApple)?.resolveLayoutParams(lp: parent.media.layoutPosition, parentSize: newSize, unitsConverter: unitsConverter, templateSize: templateSize)
        }
        parent.inspChildren.forEach { ic in
            ic.onAttach() //todo make it otherwise
            //let relativeToParent = ic.media.layoutPosition?.relativeToParent ?? true
            let dependsOnParent = ic.media.dependsOnParent ?? false
            
            (ic.view as! ViewPlatformApple).resolveLayoutParams(lp: ic.media.layoutPosition, parentSize:  CGSize(width: parent.viewWidth.cg, height: parent.viewHeight.cg), unitsConverter: unitsConverter, templateSize: templateSize)
            
            
            if (ic.hasTextureMask()) {
                let maskId = maskDictionary[ic.media.id!]
                if let maskView = allViews.first(where: {($0 as! InspView<AnyObject>).media.id == maskId}) as? InspView<AnyObject> {
                    let size = CGSize(width: ic.viewWidth.cg, height: ic.viewHeight.cg)
                    maskView.onAttach()
                    maskView.view?.viewApple.resolveLayoutParams(lp: maskView.media.layoutPosition, parentSize: size, unitsConverter: unitsConverter, templateSize: templateSize)
                }
            }
            
            if let mediav = ic.asInspMediaView() {
                
                let v = mediav.view as! ViewPlatformApple
                
                if (self.templateMode == TemplateMode.listDemo) {
                    v.demoScaleX = mediav.media.demoScale.cg
                    v.demoScaleY = mediav.media.demoScale.cg
                    v.demoTranslationX = mediav.media.demoOffsetX.cg * v.width.cg
                    v.demoTranslationY = mediav.media.demoOffsetY.cg * v.height.cg
                } else {
                    v.demoScaleX = 1.cg
                    v.demoScaleY = 1.cg
                    v.demoTranslationX = 0.cg
                    v.demoTranslationY = 0.cg
                }
                
                if let borderString = mediav.media.borderWidth {
                    (mediav.view as! ViewPlatformApple).borderWidth = unitsConverter.convertUnitToPixelsF(
                        value: borderString,
                        screenWidth: mediav.viewWidth,
                        screenHeight: mediav.viewHeight,
                        fallback: 0,
                        forHorizontal: nil
                    ).cg
                }
                
                if let innerMedia = mediav.innerMediaView as? InnerMediaViewApple {
                    innerMedia.sizeWasChanged()
                }
            }
            if let group = ic.asInspGroupView() {
                onParentSizeChangedRecursive(parent: group, newSize: CGSize(width: parent.viewWidth.cg, height: parent.viewHeight.cg))
                if let borderString = group.media.borderWidth {
                    (group.view as! ViewPlatformApple).borderWidth = unitsConverter.convertUnitToPixelsF(
                        value: borderString,
                        screenWidth: group.viewWidth,
                        screenHeight: group.viewHeight,
                        fallback: 0,
                        forHorizontal: nil
                    ).cg
                }
            }
            
            if (dependsOnParent && (parent as? InspGroupView)?.media.orientation == .z) {
                (parent as? InspGroupView)?.view?.changeSize(width: ic.viewWidth.float, height: ic.viewHeight.float)
            }
        }
    }
    
    func updateLayouts() {
        onParentSizeChangedRecursive(parent: self, newSize: getTemplateSize())
    }
    
    func updateChildrenSize(parentSize: CGSize) {
        onParentSizeChangedRecursive(parent: self, newSize: parentSize)
        return
    }
    
    override var viewWidth: Int32 { return templateWidth.toInt32 }
    
    override var viewHeight: Int32 { return templateHeight.toInt32 }
    
    override func invalidateGuidelines() {
        //todo
    }
    
    override func doInitMusicPlayer() -> BaseAudioPlayer {
        //todo
        return MusicPlayerViewModel(isPlaying: false, selectedTrack: nil)
    }
    
    override func isWindowVisible() -> Bool {
        //todo
        return true
    }
    
    override func post(action: @escaping () -> Void) {
        action()
    }
    
    override var copyInspViewPlusTranslation: Float {
        return InspTemplateView.companion.COPY_INSP_VIEW_TRANSLATION_PLUS
    }
    
    @Published
    var backgroundColor: SwiftUI.Color? = .clear
    
    @Published
    var backgroundGradient: LinearGradient? = nil
    
    override func setBackgroundColor(color_ color: Int32) {
        DispatchQueue.main.async {
            self.backgroundGradient = nil
            self.backgroundColor = color.ARGB
        }
    }
    
    override func setBackgroundColor(color: AbsPaletteColor?) {
        DispatchQueue.main.async {
            if let paletteColor = color as? PaletteColor {
                self.setBackgroundColor(color_: paletteColor.color)
            }
            
            if let gradient = color as? PaletteLinearGradient {
                self.backgroundColor = .clear
                self.backgroundGradient = gradient.getLinearGradient()
            }
        }
        
    }
    
    override func addViewToHierarchy(view: InspView<AnyObject>) {
        //print("added new view \(view)")
    }
    
    override func addViewToHierarchy(index: Int32, view: InspView<AnyObject>) {
        //todo
    }
    
    override func doCopyInspView(inspView: InspView<AnyObject>) {
        super.doCopyInspView(inspView: inspView)
        setFrameForEdit()
        objectWillChanged()
    }
    
    override func addInspView(it: Media, parentInsp: InspParent, simpleVideo: Bool) -> InspView<AnyObject> {
        let inspView = super.addInspView(it: it, parentInsp: parentInsp, simpleVideo: simpleVideo)
        if (inspView.media.textureIndex != nil) {
            (inspView.view as! ViewPlatformApple).resolveLayoutParams(lp: inspView.media.layoutPosition, parentSize: CGSize(width: parentInsp.viewWidth.cg, height: parentInsp.viewHeight.cg), unitsConverter: unitsConverter, templateSize: CGSize(width: templateWidth, height: templateHeight))
            inspView.onAttach()
            if let img = inspView.asInspMediaView() {
                if let borderString = img.media.borderWidth {
                    (img.view as! ViewPlatformApple).borderWidth = unitsConverter.convertUnitToPixelsF(
                        value: borderString,
                        screenWidth: img.viewWidth,
                        screenHeight: img.viewHeight,
                        fallback: 0,
                        forHorizontal: nil
                    ).cg
                }
            }
        }
        return inspView
    }
    
    override func applyStyleToText(existingText: InspTextView?, newMedia: Media) {
        super.applyStyleToText(existingText: existingText, newMedia: newMedia)
        updateChildrenSize(parentSize: getTemplateSize())
        objectWillChanged()
    }
    
    override func setFrame(frame: Int32) {
        if (templateVisible) {
            super.setFrame(frame: frame)
            self.objectWillChange.send()
        } else {
            if (logLevel >= 1 ) {
                print("stop playing because invisible \(debugName)")
            }
            if (isPlayingActive) {
                stopPlaying()
            }
        }
    }
    
    func getTemplateSizeInt() -> Size {
        return Size(width: templateWidth.toInt32, height: templateHeight.toInt32)
    }
    
    static func fakeInitializedTemplate() -> InspTemplateViewApple {
        let res = MR.assetsTemplatesChristmas().Ch3Presents
        let path = PredefinedTemplatePath(res: res)
        let tv = InspTemplateViewApple(templatePath: path, template: fakeTemplate())
        tv.isInitialized.setValue(true)
        return tv
    }
    
    static func fakeTemplate() -> Template {
        let res = MR.assetsTemplatesChristmas().Ch3Presents
        let templateReadWrite: TemplateReadWrite = Dependencies.resolveAuto()
        let path = PredefinedTemplatePath(res: res)
        return templateReadWrite.loadTemplateFromPath(path: path)
    }
    
    var playTimer: Timer? = nil
    
    override func startPlayingJob() {
        if (logLevel >= 2) {
            print("start playing job is visible = \(templateVisible) \(debugName)")
        }
        stopPlayingJob()
        DispatchQueue.main.async {
            self.playTimer = Timer.scheduledTimer(withTimeInterval: 1.0 / 30.0, repeats: true) { [self] timer in
                var nextFrame = currentFrame + 1
                if nextFrame >= (maxFrames - 1) && loopAnimation {
                    nextFrame = 0
                }
                if (logLevel >= 1) {
                    print("set next frame \(nextFrame) max = \(maxFrames) loop \(loopAnimation) \(debugName)")
                }
                onFrameUpdated(value: Int32(nextFrame))
            }
        }

        
        
    }
    override func stopPlayingJob() {
        if (logLevel >= 2) {
            print("stop playing job \(debugName)")
        }
        playTimer?.invalidate()
        playTimer = nil
    }
    
    deinit {
        print("deinit template \(debugName)")
    }
}

extension InspTemplateView {
    func objectWillChanged() {
        DispatchQueue.main.async {
            (self as? InspTemplateViewApple)?.objectWillChange.send()
        }
    }
    func refreshAllMedias() {
        refreshRecursive(medias: template_.medias  as NSArray as! [Media])
    }
    
    func doWhenTemplateInitialized(action: @escaping () -> Void) {
        if ((isInitialized.value as? KotlinBoolean) == true) {
            action()
        } else {
            (self as! InspTemplateViewApple).onInitializedAction = action
        }
    }
    
    func isVisible() -> Bool {
        return (self as? InspTemplateViewApple)?.templateVisible ?? false
    }
    
    func canPlayMusic() -> Bool {
        return (self as? InspTemplateViewApple)?.isMusicEnabled ?? false
    }
    
    func setLogLevel(value: Int) {
        (self as? InspTemplateViewApple)?.logLevel = value
    }
    
    var isPlayingActive: Bool {
        get {
            return (isPlaying.value as! KotlinBoolean).boolValue
        }
    }
}
