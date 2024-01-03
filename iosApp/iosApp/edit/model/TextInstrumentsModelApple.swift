//
//  TextInstrumentsModel.swift
//  iosApp
//
//  Created by rst10h on 26.01.22.
//

import Foundation
import shared

class TextInstrumentsModelApple: ObservableObject {
    let coreModel: TextInstrumentsPanelViewModel
    
    @Published
    var menu: CommonMenu<TextInstruments>
    
    let colors = BottomInstrumentColorsLight()
    let dimens = BottomInstrumentsDimensPhone()
    
    @Published
    var activeInstrument: TextInstruments?
    
    @Published
    var alignment: TextAlign
    
    init(model: TextInstrumentsPanelViewModel) {
        
        var newMenu = BottomMenuItems().menuText
        if model.getCurrentView()?.media.isCircularText() == true {
            newMenu.removeMenuItem(item: TextInstruments.textSize)
        }
        self.menu = newMenu
        
        self.coreModel = model
        self.alignment = coreModel.alignment.value as! TextAlign
        
        self.activeInstrument = coreModel.activeTextInstrument.value as? TextInstruments
        
        CoroutineUtil.watch(state: coreModel.activeTextInstrument, onValueReceived: {
            [weak self] in
                self?.activeInstrument = $0

        })
        
        CoroutineUtil.watch(state: coreModel.alignment, onValueReceived: {
            [weak self] in self?.alignment = $0
        })
        
        CoroutineUtil.onReceived(state: coreModel.currentView) {
            [weak self] in self?.onViewChanged()
        }
        
    }
    
    func onViewChanged() {
        var newMenu = BottomMenuItems().menuText
        if coreModel.getCurrentView()?.media.isCircularText() == true {
            newMenu.removeMenuItem(item: TextInstruments.textSize)
        }
        self.menu = newMenu
    }
    
    func select(_ instrument: TextInstruments) {
        coreModel.onInstrumentClick(textInstrument: instrument)
        self.objectWillChange.send()
        print("selected instrument \(instrument)")
    }
    
    func isHighlighted(_ item: TextInstruments) -> Bool {
        return coreModel.itemHighlight(item: item)
    }
}
