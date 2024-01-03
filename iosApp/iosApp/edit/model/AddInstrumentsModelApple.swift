//
//  AddInstrumentsModelApple.swift
//  iosApp
//
//  Created by rst10h on 28.06.22.
//

import Foundation
import shared
import SwiftUI

class AddInstrumentsModelApple: ObservableObject {
    
    let coreModel: AddViewsPanelModel
    let menu: CommonMenu<AddViewsInstruments>
    
    let colors = BottomInstrumentColorsLight()
    let dimens = DefaultInstrumentsDimensPhone()
    
    @Published
    var activeInstrument: AddViewsInstruments?
    
    init(model: AddViewsPanelModel) {
        
        self.coreModel = model
        
        self.activeInstrument = coreModel.activeInstrument.value as? AddViewsInstruments
        var modelMenu = model.menu
        modelMenu.removeMenuItem(item: .addLogo)
        self.menu = modelMenu
        CoroutineUtil.watch(state: coreModel.activeInstrument, allowNilState: true) {
            [weak self] in
            self?.activeInstrument = $0
            
        }
    }
    
    func select(_ instrument: AddViewsInstruments) {
        coreModel.selectInstrument(defaultInstrument: instrument)
    }
    
    func isHighlighted(_ item: AddViewsInstruments) -> Bool {
        return coreModel.itemHighlight(item: item)
    }
    
}
