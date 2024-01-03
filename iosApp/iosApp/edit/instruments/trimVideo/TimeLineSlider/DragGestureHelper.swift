//
//  DragGestureHelper.swift
//  iosApp
//
//  Created by rst10h on 27.07.22.
//

import Foundation
import CoreGraphics
import SwiftUI

class DragGestureHelper {
   
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
        
        let leftPosition = leftValue * frameWidth + Self.additionalDragArea
        let rightPosition = rightValue * frameWidth - Self.additionalDragArea
        
        let isMinimalLeft = dragLocation >= (rightPosition - Self.minimumSelectionWidth)
        let isMinimalRight = dragLocation <= (leftPosition + Self.minimumSelectionWidth)
        
        if (dragLocation < leftPosition && !isMinimalLeft && dragLocation >= leftPosition - 50) {
            print("drag leading \(dragLocation)  \(leftPosition)")
            newDragArea = .leading
        }
        
        if (dragLocation > rightPosition && !isMinimalRight && dragLocation <= rightPosition + 50) {
            print("drag trailing \(dragLocation) \(rightPosition)")
            newDragArea = .trailing
        }
        
        if (newDragArea == nil ) { //&& dragLocation > leftValue * frameWidth && dragLocation < rightValue * frameWidth
            print("drag center")
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


enum DragArea {
    case leading, center, trailing
}
