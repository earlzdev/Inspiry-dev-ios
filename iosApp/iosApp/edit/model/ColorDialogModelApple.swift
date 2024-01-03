//
//  ColorDialogModel.swift
//  iosApp
//
//  Created by rst10h on 2.02.22.
//

import Foundation
import SwiftUI
import shared

class ColorDialogModelApple: ObservableObject {
    
    let coreModel: ColorDialogViewModel
    
    let colors = TextColorDialogLightColors()
    
    let dimens = TextColorDialogDimensPhone()
    
    @Published
    var currentPage: ColorDialogPage
    
    @Published
    var colorLayerCount: Int = 0
    
    @Published
    var gradientLayerCount: Int = 0
    
    @Published
    var paletteLayerCount: Int = 0
    
    @Published
    var opacityOneLayer: Bool = false
        
    @Published
    var mediaResult: [PickMediaResult] = [] {
        didSet {
            if let res = mediaResult.first {
                coreModel.onSingleMediaSelected(uri: res.uri, isVideo: res.type == PickedMediaType.video)
                mediaResult.removeAll()
                notifyTemplateChanged()
            }
        }
    }
    
    let notifyTemplateChanged: () -> ()
    
    convenience init(_ templateView: InspTemplateView, isBack: Bool = false, isTemplate: Bool = true, notifyTemplteChanged: @escaping () -> ()) {
        
        let model = Self.getTemplateColorModel(templateView: templateView)
        
        model.doInitDefaults()
        self.init (model, notifyTemplteChanged: notifyTemplteChanged)
        
    }
    
    init(_ model: ColorDialogViewModel,  notifyTemplteChanged: @escaping () -> ()) {
        
        self.notifyTemplateChanged = notifyTemplteChanged
        self.coreModel = model
        
        currentPage = coreModel.selectedPage.value as! ColorDialogPage
        
        CoroutineUtil.watch(state: coreModel.selectedPage, onValueReceived: {
            [weak self] in self?.currentPage = $0
        })
        
        CoroutineUtil.watch(state: coreModel.colorLayerCount, onValueReceived: {
            [weak self] in self?.colorLayerCount = $0
        })
        
        CoroutineUtil.watch(state: coreModel.gradientLayerCount, onValueReceived: {
            [weak self] in self?.gradientLayerCount = $0
        })
        
        CoroutineUtil.watch(state: coreModel.paletteLayerCount, onValueReceived: {
            [weak self] in self?.paletteLayerCount = $0
        })
        
        CoroutineUtil.watch(state: coreModel.alphaOneLayer, onValueReceived: {
            [weak self] in self?.opacityOneLayer = $0
        })
    }
    
    func pageIsAvailable(_ page: ColorDialogPage) -> Bool {
        switch page {
        case .color:
            return coreModel.colorIsAvailable()
        case .gradient:
            return coreModel.gradientIsAvailable()
        case .palette:
            return coreModel.paletteIsAvailable()
        case .image:
            return coreModel.customImageChoiceIsAvailable()
        case .opacity:
            return coreModel.colorIsAvailable()
        case .roundness:
            return coreModel.hasAdditionalSliders() && coreModel.colorIsAvailable()
        default:
            return true
        }
    }
    
    func pages() -> CommonMenu<ColorDialogPage> {
        return ColorDialogViewModel.companion.pages
    }
    
    func paletteItems() -> PaletteItems {
        return coreModel.paletteItems
    }
    
    func getCurrentColorForLayer(_ layer: Int) -> Int {
        return coreModel.getCurrentColorIndexForLayer(layer: layer.int32, init: false).int
    }
    
    func getCurrentGradientForLayer(_ layer: Int) -> Int {
        return coreModel.getCurrentGradientIndexForLayer(layer: layer.int32, init: false).int
    }
    
    func getCurrentPaletteForLayer(_ layer: Int) -> Int {
        return coreModel.getCurrentPaletteIndexForLayer(layer: layer.int32, init: false).int
    }
    
    func onPageSelected(_ page: ColorDialogPage) {
        coreModel.onPageSelected(page: page)
    }
    
    func getCurrentAlphaForLayer(_ layer: Int) -> Float {
        return coreModel.getCurrentAlphaForLayer(layer: layer.int32)
    }
    
    func getAlphaLayersCount() -> Int {
        return opacityOneLayer ? 1 : colorLayerCount
    }
    
    func getcurrentImageIfAvailable() -> URL? {
        let path = coreModel.getCurrentImageBackground()
        //todo convert assets path to url
        return nil
    }
    func onPickCOlor(layer: Int, color: SwiftUI.Color) {
        let androidColor = NSNumber(value: color.toRGBInt() ?? 0)
        coreModel.onPickColor(layer: layer.int32, color: Int32(truncating: androidColor))
    }
    
    func onColorPick(layer: Int, id: Int) {
        coreModel.onColorSelected(layer: layer.int32, colorID: id.int32)
        notifyTemplateChanged()
        print("current color for layer = \(getCurrentColorForLayer(layer))")
        objectWillChange.send()
    }
    
    func onGradientPick(layer: Int, id: Int) {
        coreModel.onGradientSelected(layer: layer.int32, gradientID: id.int32)
        notifyTemplateChanged()
        objectWillChange.send()
    }
    
    func onPalettePick(id: Int) {
        print("paletteid \(id)")
        coreModel.onPaletteSelected(layer: 0, paletteID: id.int32)
        notifyTemplateChanged()
        objectWillChange.send()
    }
    
    func onAlphaChanged(layer: Int, alpha: Float) {
        coreModel.onOpacityChanged(layer: layer.int32, value: alpha)
        notifyTemplateChanged()
    }
    
    static func getTemplateColorModel(templateView: InspTemplateView) -> ColorDialogViewModel {
        return TemplatePaletteChangeViewModel(
            templateView: templateView,
            analyticsManager: Dependencies.resolveAuto(),
            json: Dependencies.resolveAuto()
        )
    }
}
