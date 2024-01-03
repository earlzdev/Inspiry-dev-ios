//
//  TouchMediaMatrixHelper.swift
//  iosApp
//
//  Created by rst10h on 7.07.22.
//

import Foundation
import SwiftUI
import UIKit
import shared

class MediaTransformHelper {
    
    var media: MediaImage? = nil
    
    var displaySize: CGSize = .zero
    var mediaSize: CGSize = .zero
    
    var dragInProgress: Bool = false
    var scaleInProgress: Bool = false
    var rotateInProgress: Bool = false
    
    var rawTransformData: TransformMediaData? = nil
    
    private var onMediaTransformChanged: (_ fromUser: Bool ) -> Void = { _ in}
    
    private func transformMedia() {
        
    }
    var baseScaleFactor = 1.cg
    
    
    func reset() {
        baseScaleFactor = 1.cg
    }
    
    func setOnMediaTransformed(action: @escaping (Bool) -> Void) {
        onMediaTransformChanged = action
    }
    
    func onFrameSizeChanged(new: CGSize) {
        if (displaySize != .zero) {
            baseScaleFactor = new.width / (displaySize.width / baseScaleFactor)
        }
        displaySize = new
    }
    
    var baseTransformData: TransformMediaData? = nil
    
    func updateBaseTransform(offsetX: CGFloat, offsetY: CGFloat, scale: CGFloat) {
        baseTransformData = TransformMediaData(scale: scale.float, translateX: offsetX.float, translateY: offsetY.float, rotate: 0)
    }
    
    func getTransform (offsetX: CGFloat, offsetY: CGFloat, scale: CGFloat, rotate: CGFloat, isDemo: Bool = false) -> TransformMediaData {
        //offset to anchor top_left
        //to make demo transformations match the demo in android
        
        let translateX, translateY: CGFloat
        
        if (isDemo) {
            let demoScaleOffsetX =  (mediaSize.width * scale - mediaSize.width) / 2
            let demoScaleOffsetY = (mediaSize.height * scale - mediaSize.height) / 2 
            translateX = demoScaleOffsetX + offsetX * displaySize.width
            translateY = demoScaleOffsetY + offsetY * displaySize.height
        } else {
            translateX = offsetX * displaySize.width
            translateY = offsetY * displaySize.height
        }
        
        return TransformMediaData(scale: scale.float * (baseTransformData?.scale ?? 1), translateX: translateX.float, translateY: translateY.float, rotate: rotate.float)
        
        
    }
    
    func onDragAction( dx: CGFloat, dy: CGFloat) {
       
//        let transform = imageView.transform
        let scalex = 1.cg
        let scaley = 1.cg
        
        let newDx = dx / displaySize.width * scalex
        let newDy = dy / displaySize.height * scaley
        
        media?.innerImageOffsetX += newDx.float
        media?.innerImageOffsetY += newDy.float
        
        onMediaTransformChanged(true)
    }
    
    func onScaleAction(scale: CGFloat) {
        let oldScale = media?.innerImageScale ?? 1
        let newScale = oldScale + scale.float
        media?.innerImageScale = newScale
        onMediaTransformChanged(true)
    }
    
    func onRotateAction(rotation: CGFloat) {
        media?.innerImageRotation += Float(Angle(radians: rotation).degrees)
        onMediaTransformChanged(true)
    }
   

}
