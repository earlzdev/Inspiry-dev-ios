//
//  FormatInstrumentModel.swift
//  iosApp
//
//  Created by rst10h on 6.02.22.
//

import Foundation
import shared

class FormatInstrumentModelApple: ObservableObject {
    
    @Published
    var currentFormat: TemplateFormat
    
    private let coreModel: FormatSelectorViewModel
       
    init(model: FormatSelectorViewModel) {
        
        self.coreModel = model
        self.currentFormat = model.currentFormat.value as! TemplateFormat
        
        CoroutineUtil.watch(state: coreModel.currentFormat) {
            [weak self] in
            self?.currentFormat = $0
        }
    }
    
    func selectFormat(_ newFormat: DisplayTemplateFormat, hasPremium: Bool) {
        print("has premium format \(hasPremium) \(newFormat.premium)")
        if (newFormat.premium && !hasPremium) {
            coreModel.onFormatChanged(newFormat: nil)
        } else {
            coreModel.onFormatChanged(newFormat: newFormat.templateFormat)
        }
    }
}
