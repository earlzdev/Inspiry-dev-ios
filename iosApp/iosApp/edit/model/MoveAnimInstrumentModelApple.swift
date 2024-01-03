//
//  MoveAnimInstrumentModelApple.swift
//  iosApp
//
//  Created by rst10h on 13.01.23.
//

import Foundation
import shared
import SwiftUI

class MoveAnimInstrumentModelApple: ObservableObject {
    
    let coreModel: MoveAnimInstrumentModel
       
    let colors = BottomInstrumentColorsLight()
    let dimens = BottomInstrumentsDimensPhone()
    
    @Published
    var activeAnim: MoveAnimations?
    
    var moveAnims: [MoveAnimations]
   
    var wrapperHelper: EditWrapperHelperApple
    
    init(model: MoveAnimInstrumentModel, wrapperHelper: EditWrapperHelperApple) {
        
        self.coreModel = model
        self.moveAnims = MoveAnimInstrumentModel.companion.animationsList
        self.wrapperHelper = wrapperHelper
        
        activeAnim = coreModel.getCurrentAnim()
               
        CoroutineUtil.watch(state: coreModel.currentView, allowNilState: false) {
            [weak self] in self?.onSelectedViewChanged(mediaView: $0)
            
        }
        coreModel.onFrameChanged = { [weak self] finished in
            let template = (self?.coreModel.currentView.value as? InspMediaView)?.templateParent
            if (template?.templateMode == .edit) {
                template?.objectWillChanged()
                withAnimation {
                    self?.wrapperHelper.isVisible = finished.boolValue
                    print("frame changed last = \(self?.wrapperHelper.isVisible)")
                }
            }
        }
    }
    
    
    
    func onSelectedViewChanged(mediaView: InspMediaView) {
        activeAnim = coreModel.getCurrentAnim()
    }
    
    func select(_ anim: MoveAnimations) {
        wrapperHelper.isVisible = false
        coreModel.selectAnimation(anim: anim)
        activeAnim = anim
        //todo
//        activeShape = ((coreModel.currentView.value as? InspMediaView)?.shapeState.value as? ShapeType) ?? ShapeType.nothing
        (coreModel.inspView.templateParent as? InspTemplateViewApple)?.objectWillChange.send()
    }
    
    
//    func isHighlighted(_ item: AddViewsInstruments) -> Bool {
//        return coreModel.itemHighlight(item: item)
//    }
    
}
