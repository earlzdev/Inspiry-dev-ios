//
//  AnimationHelperApple.swift
//  iosApp
//
//  Created by rst10h on 7.04.22.
//

import Foundation
import shared
import SwiftUI

class AnimationHelperApple: CommonAnimationHelper<AnyObject> { //todo canvas
    
    private var _clipStickingCorners: Bool = false
    
    let maskPath = ApplePath()
    
//    var _drawAnimations: NSMutableArray = NSMutableArray()
//    
//    override var drawAnimations: NSMutableArray {
//        get {
//            return _drawAnimations
//        }
//    }
//    
//    var _resetAnimations: NSMutableArray = NSMutableArray()
//    
//    override var resetAnimations: NSMutableArray {
//        get {
//            return _resetAnimations
//        }
//    }
        
    override var clipStickingCorners: Bool {
        set(value) {
            self._clipStickingCorners = value
        }
        get {
            return self._clipStickingCorners
        }
    }
    
    
    private var _inspView: InspView<AnyObject>?
    
    override var inspView: InspView<AnyObject>? {
        set(value) {
            self._inspView = value
            mayInitMaskProvider()
        }
        get {
            return self._inspView //todo
        }
    }
    
    override init(media: Media) {
        super.init(media: media)
    }
    
    override func notifyMediaRotationChanged() {
        //todo
    }
    
    override func notifyViewCornerRadiusChanged() {
        //todo
    }
    
    override func notifyViewElevationChanged() {
        //todo
    }
    
    override func drawAnimations(canvas: Any?, currentFrame: Int32) {
        //todo
    }
       
    override func mayInitMaskProvider() {
        guard let inspView = inspView else { return }
        if (media.shape == nil && clipMaskSettings.maskType == .none) {
            maskProvider = nil
            return
        }
        if (maskProvider == nil) {
            maskProvider = MaskProvider(maskPath: maskPath, width: inspView.viewWidth, height: inspView.viewHeight)
        }
        if (maskProvider?.height != inspView.viewHeight || maskProvider?.width != inspView.viewWidth) {
            maskProvider?.updateSize(width: inspView.viewWidth, height: inspView.viewHeight)
        }
    }
    
    override func preDrawAnimations(currentFrame: Int32) {
        super.preDrawAnimations(currentFrame: currentFrame)
        
    }
    
    func notSingularMask() -> Bool {
        guard clipMaskSettings.maskType != .none else { return true }
        let notSingularScale = clipMaskSettings.shapeTransform.scaleHeight > 0 && clipMaskSettings.shapeTransform.scaleHeight > 0
        let notSingularRadus: Bool
        
        if (clipMaskSettings.maskType == .circular) {
            notSingularRadus = clipMaskSettings.radius > 0
        } else {
            notSingularRadus = true
        }
        
        return notSingularScale && notSingularRadus
        
//        return (maskProvider?.lastShapeBounds.width() ?? 1) > 0 && (maskProvider?.lastShapeBounds.height() ?? 1) > 0
    }
}

