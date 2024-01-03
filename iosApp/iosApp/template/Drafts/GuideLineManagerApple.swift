//
//  GuideLineManagerApple.swift
//  iosApp
//
//  Created by rst10h on 27.01.22.
//

import Foundation
import shared

class GuideLineManagerApple: GuidelineManager {
    
    override var guidelineThreshold: Float {
        return GuidelineManager.companion.GUIDELINE_THRESHOLD
    }
    
    override var guidelineThickness: Float {
        return GuidelineManager.companion.GUIDELINE_THICKNESS
    }
    
    override func doInitGuideline(root: InspTemplateView, targetEdge: Alignment, align: Alignment, orientation: GuideLine.Orientation, offset: Int32) -> GuideLine {
        return GuideLineApple(root: root, targetEdge: targetEdge, align: align, orientation: orientation, offset: offset)
    }
}
