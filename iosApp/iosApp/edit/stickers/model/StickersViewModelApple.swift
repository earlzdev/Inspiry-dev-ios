//
//  StickersViewModelApple.swift
//  iosApp
//
//  Created by rst10h on 6.09.22.
//

import Foundation
import shared

class StickersViewModelApple: ObservableObject {
    
    private static let instance: StickersViewModelApple = {
        return StickersViewModelApple()
    }()
    
    static func SharedInstance() -> StickersViewModelApple {
        if (instance.currentCategory != instance.categories?.first) {
            instance.loadStickersForCategory(category: instance.categories?.first ?? "social")
        }
        return instance
    }
    
    var coreModel: StickersViewModel? = nil
    
    @Published
    var categories: [String]? = nil
    
    @Published
    var currentCategory: String? = nil
    
    @Published
    var currentStickers: [MediaWithPath] = []
    
    @Published
    var currentStickerIndex: Int? = nil
    
    init() {
        let provider: StickersProvider = Dependencies.resolveAuto()
        print("create stickers model")
        DispatchQueue.global().async { [self] in
            coreModel = StickersViewModel(provider: provider, initialCategory: nil, initialStickerIndex: -1)
            DispatchQueue.main.async {
                self.categories = provider.getCategories()
            }

            CoroutineUtil.watch(state: coreModel!.currentCategory, onValueReceived: { [weak self] in
                self?.currentCategory = $0
            })
            CoroutineUtil.watch(state: coreModel!.currentStickerIndex, onValueReceived: { [weak self] in
                self?.currentStickerIndex = $0
            })
            CoroutineUtil.onReceived(state: coreModel!.currentStickers, onValueReceived: {[weak self] in
                self?.updateStickers()
            })
        }
    }
    
    var templatesWithStickers: [String: InspTemplateViewApple] = [:]
    
    private func updateStickers() {
        DispatchQueue.global().async { [self] in
            if let stickers = coreModel?.getCurrentStickers() {
                for sticker in stickers {
                    sticker.media.touchActions = [.buttonClose, .buttonScale, .buttonDuplicate, .buttonRotate, .move]
                    if (templatesWithStickers[sticker.path] == nil) {
                        let template = Template(medias: [sticker.media], preferredDuration: 10000)
                        template.format = .square
                        let templateView = InspTemplateViewApple(templatePath: nil, template: template)
                        templateView.debugName = sticker.path.getFileName()
                        templateView.templateMode = TemplateMode.preview
                        templateView.shouldHaveBackground = false
                        templatesWithStickers[sticker.path] = templateView
                        DispatchQueue.main.async {
                            templateView.loadTemplate(fromPath: false)
                        }
                        print("debugstick page template init.. \(templateView.debugName)")
                    }
                }
                print("debugstick page loaded")
                DispatchQueue.main.async {
                    currentStickers = stickers
                }
            } else {
                DispatchQueue.main.async {
                    self.currentStickers = []
                }

            }
        }
    }
    
    func clearCache() {
//        templatesWithStickers.values.forEach { tv in
//            tv.unloadTemplate()
//        }
//        templatesWithStickers.removeAll()
    }
    
    func loadStickersForCategory(category: String) {
        DispatchQueue.main.async {
            self.currentStickers = []
        }
        DispatchQueue.global().async { [self] in
            coreModel?.load(category: category, resetIndex: true)
        }
    }
    
    func localizeTab(category: String) -> String {
        switch(category) {
        case ArrowStickerCategory.shared.stickersId:
            return MR.strings().string_category_arrow.localized()
        case BeautyStickerCategory.shared.stickersId:
            return MR.strings().category_beauty.localized()
        case BrushStickerCategory.shared.stickersId:
            return MR.strings().sticker_category_brush.localized()
        case PaperStickerCategory.shared.stickersId:
            return MR.strings().category_paper.localized()
        case SocialStickerCategory.shared.stickersId:
            return MR.strings().sticker_category_social.localized()
        case HalloweenStickerCategory.shared.stickersId:
            return MR.strings().category_halloween.localized()
        case ChristmasStickerCategory.shared.stickersId:
            return MR.strings().category_christmas.localized()
        default:
            fatalError("unknown sticker category: \(category)")
            
        }
    }
}
