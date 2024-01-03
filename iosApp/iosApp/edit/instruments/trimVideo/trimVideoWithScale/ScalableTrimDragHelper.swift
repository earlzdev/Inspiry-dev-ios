//
//  ScallableDragGestureHelper.swift
//  iosApp
//
//  Created by rst10h on 14.10.22.
//

import Foundation
import CoreGraphics
import SwiftUI

class ScalableTrimDragHelper {
   
    static let additionalDragArea: CGFloat = 30
    static let minimumSelectionWidth: CGFloat = 1
    
    var dx: CGFloat = 0
    var dy: CGFloat = 0
    
    var lastTranslation: CGSize = .zero
    
    private var dragArea: DragArea? = nil
      
    func newTranslation(_ translation: CGSize) {
        dx = lastTranslation.width - translation.width
        dy = lastTranslation.height - translation.height
        lastTranslation = translation
    }
    
    func horizontalDrag(dragLocation: CGFloat, frameWidth: CGFloat, leftValue: CGFloat, rightValue: CGFloat) -> DragArea? {
        
        var newDragArea: DragArea? = nil
        
        let leftPosition = (ScalableTrimSlider.horizontalSpacing - ScalableTrimSlider.thumbnailsPadding - 20)...ScalableTrimSlider.horizontalSpacing + 20
        let rightPosition = (frameWidth - ScalableTrimSlider.horizontalSpacing - 20)...(frameWidth - ScalableTrimSlider.horizontalSpacing + ScalableTrimSlider.thumbnailsPadding + 20)
               
        if (leftPosition.contains(dragLocation)  ) {
            print("drag leading \(dragLocation)  \(leftPosition)")
            newDragArea = .leading
        }
                
        if (rightPosition.contains(dragLocation)) {
            print("drag trailing \(dragLocation) \(rightPosition)")
            newDragArea = .trailing
        }
        
        if (newDragArea == nil ) { //&& dragLocation > leftValue * frameWidth && dragLocation < rightValue * frameWidth
            newDragArea = .center
        }
        
        dragArea = newDragArea
        
        if (dragArea != newDragArea) { return nil }
        
        return dragArea
    }
    
    func dragEnded() {
        dx = 0
        dy = 0
        lastTranslation = .zero
        dragArea = nil
    }
}
