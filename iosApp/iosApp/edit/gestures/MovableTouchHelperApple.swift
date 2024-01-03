//
//  MovableTouchHelperApple.swift
//  iosApp
//
//  Created by rst10h on 27.01.22.
//

import Foundation
import shared
import UIKit
import SwiftUI

class MovableTouchHelperApple: MovableTouchHelper {
    
    let guideLineManager: GuidelineManager
    
    override var viewMovedThreshold: Float {
        return 6
    }
    
   
    init (inspView: InspView<AnyObject>, guideLine: GuidelineManager) {
        self.guideLineManager = guideLine
        super.init(inspView: inspView, guidelineManager: guideLine)
    }
   
    func onDragEvent(dx: CGFloat, dy: CGFloat) {
        
        inspView.setSelected()
        
        let rotation = Angle(degrees: inspView.getAbsoluteRotation().double)
        
        let delta = CGPoint(x: dx, y: dy).rotation(rotation)
          
        mayPerformMovement(delta: delta.x.float, canMove: inspView.media.canMoveX(), forX: true, parentSize: inspView.templateParent.viewWidth)
        mayPerformMovement(delta: delta.y.float, canMove: inspView.media.canMoveY(), forX: false, parentSize: inspView.templateParent.viewHeight)
        inspView.templateParent.objectWillChanged()
    }
}
