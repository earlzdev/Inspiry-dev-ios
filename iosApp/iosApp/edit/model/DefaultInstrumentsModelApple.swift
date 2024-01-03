//
//  DefaultInstrumentsModel.swift
//  iosApp
//
//  Created by rst10h on 26.01.22.
//

import Foundation
import shared
import SwiftUI

class DefaultInstrumentsModelApple: ObservableObject {
    
    let coreModel: DefaultInstrumentsPanelViewModel
    let menu: CommonMenu<DefaultInstruments>
    
    let colors = BottomInstrumentColorsLight()
    let dimens = DefaultInstrumentsDimensPhone()
    
    @Published
    var activeInstrument: DefaultInstruments?
    
    init(model: DefaultInstrumentsPanelViewModel) {
        
        self.coreModel = model
        
        self.activeInstrument = coreModel.activeInstrument.value as? DefaultInstruments
        self.menu = model.menu
        CoroutineUtil.watch(state: coreModel.activeInstrument, allowNilState: true) {
            [weak self] in
            self?.activeInstrument = $0
            
        }
    }
    
    func select(_ instrument: DefaultInstruments) {
        coreModel.selectInstrument(defaultInstrument: instrument)
    }
    
    func isHighlighted(_ item: DefaultInstruments) -> Bool {
        return coreModel.itemHighlight(item: item)
    }
    
}
