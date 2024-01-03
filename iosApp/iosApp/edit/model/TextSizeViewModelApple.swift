//
//  TextSizeViewModelApple.swift
//  iosApp
//
//  Created by rst10h on 11.03.22.
//

import Foundation
import shared

class TextSizeViewModelApple: ObservableObject {
    
    var coreModel: TextSizeInstrumentViewModel? = nil
    
    @Published
    var textSize: Float {
        didSet {
            onTextSizeChanged(value: textSize)
        }
    }
    
    @Published
    var letterSpacing: Float {
        didSet {
            onLetterSpacingChanged(value: letterSpacing)
        }
    }
    
    @Published
    var lineSpacing: Float {
        didSet {
            onLineSpacingChanged(value: lineSpacing)
        }
    }
    
    let colors = SizeInstrumentColorsLight()
    let dimens = SizeInstrumentsDimensPhone()
    
    init(model: TextSizeInstrumentViewModel) {
        coreModel = model
        
        textSize = model.textSizeState.value as! Float
        letterSpacing = model.letterSpacingState.value as! Float
        lineSpacing = model.lineSpacingState.value as! Float

//        CoroutineUtil.watch(state: coreModel!.textSizeState) {
//            [weak self] in
//            self?.textSize = $0
//        }
//        CoroutineUtil.watch(state: coreModel!.letterSpacingState) {
//            [weak self] in
//            self?.letterSpacing = $0
//        }
//        CoroutineUtil.watch(state: coreModel!.lineSpacingState) {
//            [weak self] in
//            self?.lineSpacing = $0
//        }
    }
    
    func onTextSizeChanged(value: Float) {
        coreModel?.onTextSizeChanged(value: value)
        updateTextLayout()

    }
    
    private func updateTextLayout() {
        guard let inspView = coreModel?.selectedView.value as? InspTextView else { return }
        inspView.textView?.requestLayout()
        inspView.templateParent.onSelectedViewMovedListener?()
        inspView.templateParent.objectWillChanged()
    }
    
    func onLetterSpacingChanged(value: Float) {
        coreModel?.onLetterSpacingChanged(value: value)
        updateTextLayout()
    }
    
    func onLineSpacingChanged(value: Float) {
        coreModel?.onLineSpacingChanged(value: value)
        updateTextLayout()
    }
    
    init() {
        textSize = 0.7
        letterSpacing = 0.3
        lineSpacing = 0.5
    }
}
