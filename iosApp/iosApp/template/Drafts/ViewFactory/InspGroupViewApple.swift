//
//  InspGroupViewApple.swift
//  iosApp
//
//  Created by rst10h on 27.01.22.
//

import Foundation
import SwiftUI
import shared

//class InspGroupViewApple: InspGroupView {
//    
//    init( media: MediaGroup,
//          parentInsp: InspParent?,
//          view: ViewPlatformApple?,
//          unitsConverter: BaseUnitsConverter,
//          animationHelper: AbsAnimationHelper?,
//          loggerGetter: LoggerGetter,
//          touchHelperFactory: MovableTouchHelperFactory,
//          templateParent: InspTemplateView) {
//        super.init( media: media,
//                    parentInsp: parentInsp,
//                    view: view,
//                    unitsConverter: unitsConverter,
//                    animationHelper: animationHelper,
//                    loggerGetter: loggerGetter,
//                    touchHelperFactory: touchHelperFactory,
//                    templateParent: templateParent)
//        
//    }
//    
//    
//    override func addViewToHierarchy(view: InspView<AnyObject>) {
//        super.addViewToHierarchy(view: view)
//    }
//    
//    override func onCurrentFrameChanged(newVal: Int32, oldVal: Int32) {
//        super.onCurrentFrameChanged(newVal: newVal, oldVal: oldVal)
//    }
//    
//    
//    deinit {
//        print("deinit group")
//    }
//}

extension InspGroupView {
    func onChildSizeChanged(newSize: CGSize) {
        
        if (media.orientation == GroupOrientation.z) {
            view?.changeSize(width: newSize.width.float, height: newSize.height.float)
        }
        
        if (media.orientation == GroupOrientation.v) {
            var fullHeight: CGFloat = 0
            children.forEach { child in
                let vp = (child as! InspView<AnyObject>).view as! ViewPlatformApple
                fullHeight = fullHeight + (child as! InspView<AnyObject>).viewHeight.cg + vp.marginTop + vp.marginBottom + vp.paddTop + vp.paddBottom
                
            }
            view?.changeSize(width: newSize.width.float, height: fullHeight.float)
        }
        
        if (media.orientation == GroupOrientation.h) {
            var fullWidth: CGFloat = 0
            children.forEach { child in
                let vp = (child as! InspView<AnyObject>).view as! ViewPlatformApple
                fullWidth = fullWidth + (child as! InspView<AnyObject>).viewWidth.cg + vp.marginLeft + vp.marginRight + vp.paddLeft + vp.paddRight
            }
            view?.changeSize(width: fullWidth.float, height: newSize.height.float)
        }
        
        (templateParent as! InspTemplateViewApple).onParentSizeChangedRecursive(parent: self, newSize: CGSize(width: parentInsp?.viewWidth.cg ?? 0.cg, height: parentInsp?.viewHeight.cg ?? 0.cg))
        (templateParent as! InspTemplateViewApple).objectWillChange.send()
    }
}

