//
//  FontViewModelApple.swift
//  iosApp
//
//  Created by vlad on 14/7/21.
//

import Foundation
import shared

class FontsViewModelApple: ViewModelBridge<FontsViewModel> {
    
    @Published
    public private(set) var currentFontStyle: InspFontStyle
    
    @Published
    public private(set) var currentFontPath: FontPath
    
    @Published
    public private(set) var currentText: String
    
    @Published
    public private(set) var currentCategoryIndex: Int
    
    @Published
    public private(set) var currentFonts : InspResponse<FontPathsResponse>
    
    override init(_ coreModel: FontsViewModel) {
        
        currentFontStyle = coreModel.currentFontStyle.value as! InspFontStyle
        currentFontPath = coreModel.currentFontPath.value as! FontPath
        currentText = coreModel.currentText.value as! String
        currentCategoryIndex = coreModel.currentCategoryIndex.value as! Int
        currentFonts = coreModel.currentFonts.value as! InspResponse<FontPathsResponse>
        
        super.init(coreModel)
        
        CoroutineUtil.watch(state: coreModel.currentFontStyle, onValueReceived: { [weak self] in self?.currentFontStyle = $0 })
        CoroutineUtil.watch(state: coreModel.currentFontPath, onValueReceived: { [weak self] in self?.currentFontPath = $0 })
        CoroutineUtil.watch(state: coreModel.currentText, onValueReceived: { [weak self] in self?.currentText = $0 })
        CoroutineUtil.watch(state: coreModel.currentCategoryIndex, onValueReceived: { [weak self] in self?.currentCategoryIndex = $0 })
        CoroutineUtil.watch(state: coreModel.currentFonts, onValueReceived: { [weak self] in self?.currentFonts = $0 })

    }
    
    func updateLayout() {
        let inspview = coreModel.selectedView.value as? InspTextView
        inspview?.textView?.requestLayout()
        inspview?.templateParent.onSelectedViewMovedListener?()
        inspview?.templateParent.objectWillChanged()
    }
}
