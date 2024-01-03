//
//  GuideLineApple.swift
//  iosApp
//
//  Created by rst10h on 27.01.22.
//

import Foundation
import shared

class GuideLineApple: GuideLine {
    
    override init(root: InspTemplateView, targetEdge: Alignment, align: Alignment, orientation: GuideLine.Orientation, offset: Int32 = 0) {
        super.init(root: root, targetEdge: targetEdge, align: align, orientation: orientation, offset: offset)
    }
    
    override func getPositionInParent(parent: InspTemplateView, child: InspView<AnyObject>) -> KotlinIntArray {
        //todo
        return KotlinIntArray(size: 2)
    }
}
