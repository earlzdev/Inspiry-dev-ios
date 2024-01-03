//
//  SwiftUIToImageRenderer.swift
//  iosApp
//
//  Created by rst10h on 3.08.22.
//

import Foundation
import SwiftUI
import UIKit
import AVFoundation


class ViewToImageRenderer<Content> where Content: View {
    let viewSize: CGSize
    private let controller: UIHostingController<Content>
    private let renderer: UIGraphicsImageRenderer
    
    var uiView: UIView {
        return controller.view
    }
    
    init(size: CGSize, @ViewBuilder content: () -> Content) {
        self.viewSize = size
        self.controller = UIHostingController(rootView: content(), ignoreSafeArea: true)
        let format = UIGraphicsImageRendererFormat(for: UITraitCollection(displayScale: 1))
        self.renderer = UIGraphicsImageRenderer(size: size, format: format)
        controller.view.bounds = CGRect(origin: .zero, size: size)
    }
    
    func renderImage() -> UIImage {
        return renderer.image { context in
            controller.view.drawHierarchy(in: CGRect(origin: .zero, size: viewSize), afterScreenUpdates: true)
        }
    }
}

extension UIHostingController {
    convenience public init(rootView: Content, ignoreSafeArea: Bool) {
        self.init(rootView: rootView)
        
        if ignoreSafeArea {
            disableSafeArea()
        }
    }
    
    private func disableSafeArea() {
        guard let viewClass = object_getClass(view) else { return }
        
        let viewSubclassName = String(cString: class_getName(viewClass)).appending("_IgnoreSafeArea")
        if let viewSubclass = NSClassFromString(viewSubclassName) {
            object_setClass(view, viewSubclass)
        }
        else {
            guard let viewClassNameUtf8 = (viewSubclassName as NSString).utf8String else { return }
            guard let viewSubclass = objc_allocateClassPair(viewClass, viewClassNameUtf8, 0) else { return }
            
            if let method = class_getInstanceMethod(UIView.self, #selector(getter: UIView.safeAreaInsets)) {
                let safeAreaInsets: @convention(block) (AnyObject) -> UIEdgeInsets = { _ in
                    return .zero
                }
                class_addMethod(viewSubclass, #selector(getter: UIView.safeAreaInsets), imp_implementationWithBlock(safeAreaInsets), method_getTypeEncoding(method))
            }
            
            objc_registerClassPair(viewSubclass)
            object_setClass(view, viewSubclass)
        }
    }
}


extension UIView {

    func drawToImage() -> UIImage {

        
        // Begin context
        UIGraphicsBeginImageContextWithOptions(self.bounds.size, false,  0)
        if let context = UIGraphicsGetCurrentContext() {
            context.interpolationQuality = .none
        }
        // Draw view in that context
        drawHierarchy(in: self.bounds, afterScreenUpdates: true)

        //get image
        let image = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        if (image != nil)
        {
            return image!
        }
        return UIImage()
    }
}


struct PreparedFrame {
    let imageFrame: UIImage
    let pixelBuffer: CVPixelBuffer
}
