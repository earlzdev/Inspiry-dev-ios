//
//  MediaInstrumentsModelApple.swift
//  iosApp
//
//  Created by rst10h on 2.07.22.
//

import Foundation
import shared
import SwiftUI

class MediaInstrumentsModelApple: ObservableObject {
    
    let coreModel: MediaInstrumentsPanelViewModel
       
    let colors = BottomInstrumentColorsLight()
    let dimens = BottomInstrumentsDimensPhone()
    
    @Published
    var activeInstrument: MediaInstrumentType?
    
    @Published
    var menu: CommonMenu<MediaInstrumentType>
    
    init(model: MediaInstrumentsPanelViewModel) {
        
        self.coreModel = model
        
        self.activeInstrument = coreModel.activeMediaInstrument.value as? MediaInstrumentType
        self.menu = model.menu.value as! CommonMenu<MediaInstrumentType>
        
        CoroutineUtil.watch(state: coreModel.activeMediaInstrument, allowNilState: true) {
            [weak self] in
            self?.activeInstrument = $0
            
        }
        
        CoroutineUtil.watch(state: coreModel.menu, onValueReceived: {
            [weak self] in self?.menu = $0
        })
    }
    
    func select(_ instrument: MediaInstrumentType) {
        coreModel.selectInstrument(mediaInstrument: instrument)
    }
    
//    func isHighlighted(_ item: AddViewsInstruments) -> Bool {
//        return coreModel.itemHighlight(item: item)
//    }
    
}
