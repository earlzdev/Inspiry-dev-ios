//
//  ShapeInstrumentModelApple.swift
//  iosApp
//
//  Created by rst10h on 4.07.22.
//

import Foundation
import SwiftUI
import shared

class ShapeInstrumentsModelApple: ObservableObject {
    
    let coreModel: ShapesInstrumentViewModel
       
    let colors = BottomInstrumentColorsLight()
    let dimens = BottomInstrumentsDimensPhone()
    
    @Published
    var activeShape: ShapeType?
    
    var shapes: [ShapeType]
   
    init(model: ShapesInstrumentViewModel) {
        
        self.coreModel = model
        self.shapes = ShapesInstrumentViewModel.companion.shapesList
        
        activeShape = ((model.currentView.value as? InspMediaView)?.shapeState.value as? ShapeType) ?? ShapeType.nothing
               
        CoroutineUtil.watch(state: coreModel.currentView, allowNilState: false) {
            [weak self] in self?.onSelectedViewChanged(mediaView: $0)
            
        }
        
    }
    
    func onSelectedViewChanged(mediaView: InspMediaView) {
        activeShape = ((coreModel.currentView.value as? InspMediaView)?.shapeState.value as? ShapeType) ?? ShapeType.nothing
    }
    
    func select(_ shape: ShapeType) {
        coreModel.selectShape(shape: shape)
        activeShape = ((coreModel.currentView.value as? InspMediaView)?.shapeState.value as? ShapeType) ?? ShapeType.nothing
        (coreModel.inspView.templateParent as? InspTemplateViewApple)?.objectWillChange.send()
    }
    
    
//    func isHighlighted(_ item: AddViewsInstruments) -> Bool {
//        return coreModel.itemHighlight(item: item)
//    }
    
}
