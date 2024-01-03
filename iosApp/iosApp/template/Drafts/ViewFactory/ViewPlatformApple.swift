//
//  testViewPlatform.swift
//  iosApp
//
//  Created by rst10h on 27.01.22.
//

import Foundation
import shared
import UIKit
import SwiftUI

class ViewPlatformApple: ViewPlatform { //maybe observable not need..
    
    var height: Int32 = 0 //todo CGFloat
    
    var width: Int32 = 0 //todo CGFloat
    
    var widthFactor = 1.cg //for size animation
    
    var heightFactor = 1.cg //for size animation
    
    var alignCorrectionX: CGFloat = 0 //alignment shift
    
    var alignCorrectionY: CGFloat = 0 // alignment shift
    
    var onAttachListener: (() -> Void)? = nil
    
    var onDetachListener: (() -> Void)? = nil
    
    var onSizeChangeListener: ((KotlinInt, KotlinInt, KotlinInt, KotlinInt) -> Void)? = nil
    
    var paddingBottom: Int32 = 0 //this not used
    var paddingLeft: Int32 = 0
    var paddingRight: Int32 = 0
    var paddingTop: Int32 = 0
    
    //view rotation
    var rotation: Float = 0
    
    //view scale
    var scaleX: Float = 1
    var scaleY: Float = 1
    
    //translation (for animation too)
    var translationX: Float = 0
    var translationY: Float = 0
    
    //inner transforms
    var innerScaleX: CGFloat = 1
    var innerScaleY: CGFloat = 1
    var innerTranslationX: CGFloat = 0
    var innerTranslationY: CGFloat = 0
    var innerRotation: CGFloat = 0
    
    var demoScaleX: CGFloat = 1
    var demoScaleY: CGFloat = 1
    var demoTranslationX: CGFloat = 0
    var demoTranslationY: CGFloat = 0
    
    //view border
    var borderWidth: CGFloat = 0
    var borderColor: SwiftUI.Color = .white
    
    var viewAlpha: CGFloat = 1 //opacity
    
    var x: Float = 0 //from media.x
    
    var y: Float = 0 //from media.y
    
    private var alignMultiplierX: CGFloat = 0
    private var alignMultiplierY: CGFloat = 0
    
    var xOffset: CGFloat { //view position X
        return x.cg  + translationX.cg + alignCorrectionX + alignMultiplierX * (width.cg - widthFactor * width.cg) / 2.cg
    }
    
    
    var yOffset: CGFloat { //view position Y
        return y.cg + translationY.cg + alignCorrectionY + alignMultiplierY * (height.cg - heightFactor * height.cg) / 2.cg
    }
    
    var scaledWidth: CGFloat {
        return width.cg * widthFactor
    }
    
    var scaledHeight: CGFloat {
        return height.cg * heightFactor
    }
    
    //anchor based on align
    var anchor: UnitPoint = .topLeading
    
    //padding cg
    var paddTop = 0.cg {
        didSet {
            paddingTop = paddTop.toInt32
        }
    }
    var paddLeft = 0.cg {
        didSet {
            paddingLeft = paddLeft.toInt32
        }
    }
    var paddBottom = 0.cg {
        didSet {
            paddingBottom = paddBottom.toInt32
        }
    }
    var paddRight = 0.cg {
        didSet {
            paddingRight = paddRight.toInt32
        }
    }
    
    var paddHorizontal: CGFloat {
         get {
             return paddLeft + paddRight
         }
     }
     var paddVertical: CGFloat {
         get {
             return paddTop + paddBottom
         }
     }

     var widthWPadH: CGFloat {
         get {
             return scaledWidth - paddHorizontal
         }
     }

     var heightWPadV: CGFloat {
         get {
             return scaledHeight - paddVertical
         }
     }
    
    var backgroundColor: SwiftUI.Color = Color.clear
    var backgroundGradient: LinearGradient? = nil
    
    func changeSize(width: Float, height: Float) {
        self.width = Int32(width)
        self.height = Int32(height)
    }
    
    func clickZoneIncrease(addleft: Int32, addright: Int32, addTop: Int32, addBottom: Int32) {
        
    }
    
    func doOnPreDraw(action: @escaping () -> Void) {
        
    }
    
    func hideView() {
        
    }
    
    func invalidateRotationParentChanged() {
        
    }
    
    func setAlpha(alpha: Float) {
        viewAlpha = alpha.cg
    }
    
    func setBackground(media: Media) {
        if (media.backgroundGradient != nil) {
            setBackground(gradient: media.backgroundGradient!)
        } else {
            setBackgroundColor(color_: media.backgroundColor)
            backgroundGradient = nil
        }
    }
    
    func setBackground(gradient: PaletteLinearGradient) {
        backgroundGradient = gradient.getLinearGradient()
    }
    
    func setBackgroundColor(color_ color: Int32) {
        backgroundColor = color.ARGB
    }
    
    func setElevation(value: Float) {
        
    }
    
    var marginTop: CGFloat = 0
    var marginLeft: CGFloat = 0
    var marginBottom: CGFloat = 0
    var marginRight: CGFloat = 0
    
    func setMargin(layoutPosition: LayoutPosition, parentWidth: Int32, parentHeight: Int32, unitsConverter: BaseUnitsConverter) {
        marginTop = unitsConverter.convertUnitToPixelsF(value: layoutPosition.marginTop, screenWidth: parentWidth, screenHeight: parentHeight, fallback: 0, forHorizontal: false).cg
        marginLeft = unitsConverter.convertUnitToPixelsF(value: layoutPosition.marginLeft, screenWidth: parentWidth, screenHeight: parentHeight, fallback: 0, forHorizontal: true).cg
        marginBottom = unitsConverter.convertUnitToPixelsF(value: layoutPosition.marginBottom, screenWidth: parentWidth, screenHeight: parentHeight, fallback: 0, forHorizontal: false).cg
        marginRight = unitsConverter.convertUnitToPixelsF(value: layoutPosition.marginRight, screenWidth: parentWidth, screenHeight: parentHeight, fallback: 0, forHorizontal: false).cg
        
    }
    
    func setPadding(layoutPosition: LayoutPosition, parentWidth: Int32, parentHeight: Int32, unitsConverter: BaseUnitsConverter) {
        
    }
    
    func setSizeFromAnimation(widthFactor: Float, heightFactor: Float) {
        //todo
        if (widthFactor != -1 ) {self.widthFactor = widthFactor.cg }
        if (heightFactor != -1) {self.heightFactor = heightFactor.cg}
    }
    
    func showView() {
        
    }
    
    func vibrateOnGuideline() {
    }
    
    func getCGSize() -> CGSize {
        return CGSize(width: width.cg, height: height.cg)
    }
    
    func setSize(width: Int32, height: Int32) {
        let oldWidth = self.width
        let oldHeight = self.height
        
        self.width = width
        self.height = height
        
        print("view size set new")
        
        if let listener = onSizeChangeListener {
            listener(self.width.toKotlinInt, self.height.toKotlinInt, oldWidth.toKotlinInt, oldHeight.toKotlinInt)
            print("view size listener update")
        }
    }
    
    func getCGRect() -> CGRect {
        return CGRect(x: x.cg, y: y.cg, width: width.cg, height: height.cg)
    }
    
    public func resolveLayoutParams(lp: LayoutPosition, parentSize: CGSize, unitsConverter: BaseUnitsConverter, templateSize: CGSize) {
        //todo add relative to parent
        let masterSize = lp.relativeToParent ? parentSize : templateSize
        
        var w = Int32(unitsConverter.convertUnitToPixelsF(value: lp.width, screenWidth: masterSize.width.toInt32, screenHeight: masterSize.height.toInt32, fallback: 0, forHorizontal: true))
        var h = Int32(unitsConverter.convertUnitToPixelsF(value: lp.height, screenWidth: masterSize.width.toInt32, screenHeight: masterSize.height.toInt32, fallback: 0, forHorizontal: false))
        if (w.float == SharedConstants.shared.MATCH_PARENT) { w = parentSize.width.toInt32 }
        if (h.float == SharedConstants.shared.MATCH_PARENT) { h = parentSize.height.toInt32 }
        
        if (w.float == SharedConstants.shared.WRAP_CONTENT) { w = width}
        if (h.float == SharedConstants.shared.WRAP_CONTENT) { h = height}
        
        updateViewSizeAndAlignment(lp: lp, parentSize: parentSize, unitsConverter: unitsConverter, templateSize: templateSize, newSize: Size(width: w, height: h))
    }
    
    func updateViewSizeAndAlignment(lp: LayoutPosition, parentSize: CGSize, unitsConverter: BaseUnitsConverter, templateSize: CGSize, newSize: Size) {
        
        let masterSize = lp.relativeToParent ? parentSize : templateSize
        
        let oldWidth = width
        let oldHeight = height
        
        width = newSize.width
        height = newSize.height
        
        
        anchor = lp.alignBy.unitPoint
        alignMultiplierX = (anchor.x * 2 - 1)
        alignMultiplierY = (anchor.y * 2 - 1)
        
        if (lp.relativeToParent) {
            x = (alignMultiplierX != 0 ? -alignMultiplierX.float : 1) * unitsConverter.convertUnitToPixelsF(value: lp.x, screenWidth: masterSize.width.toInt32, screenHeight: masterSize.height.toInt32, fallback: 0, forHorizontal: true)
            y = (alignMultiplierY != 0 ? -alignMultiplierY.float : 1) * unitsConverter.convertUnitToPixelsF(value: lp.y, screenWidth: masterSize.width.toInt32, screenHeight: masterSize.height.toInt32, fallback: 0, forHorizontal: false)
        } else {
            x = (alignMultiplierX != 0 ? -alignMultiplierX.float : 1) * unitsConverter.convertUnitToPixelsF(value: lp.x, screenWidth: templateSize.width.toInt32, screenHeight: templateSize.height.toInt32, fallback: 0, forHorizontal: true)
            y = (alignMultiplierY != 0 ? -alignMultiplierY.float : 1) * unitsConverter.convertUnitToPixelsF(value: lp.y, screenWidth: templateSize.width.toInt32, screenHeight: templateSize.height.toInt32, fallback: 0, forHorizontal: false)
        }
        
        alignCorrectionY = (parentSize.height - height.cg)/2.cg * alignMultiplierY
        alignCorrectionX = (parentSize.width -  width.cg)/2.cg * alignMultiplierX
        
        paddLeft = unitsConverter.convertUnitToPixelsF(value: lp.paddingStart, screenWidth: templateSize.width.toInt32, screenHeight: templateSize.height.toInt32, fallback: 0, forHorizontal: true).cg
        paddRight = unitsConverter.convertUnitToPixelsF(value: lp.paddingEnd, screenWidth: templateSize.width.toInt32, screenHeight: templateSize.height.toInt32, fallback: 0, forHorizontal: true).cg
        paddBottom = unitsConverter.convertUnitToPixelsF(value: lp.paddingBottom, screenWidth: templateSize.width.toInt32, screenHeight: templateSize.height.toInt32, fallback: 0, forHorizontal: false).cg
        paddTop = unitsConverter.convertUnitToPixelsF(value: lp.paddingTop, screenWidth: templateSize.width.toInt32, screenHeight: templateSize.height.toInt32, fallback: 0, forHorizontal: false).cg
        
        setMargin(layoutPosition: lp, parentWidth: parentSize.width.toInt32, parentHeight: parentSize.height.toInt32, unitsConverter: unitsConverter)
        
        if let listener = onSizeChangeListener {
            listener(width.toKotlinInt, height.toKotlinInt, oldWidth.toKotlinInt, oldHeight.toKotlinInt)
        }
    }
    
    func paddingBounds() -> CGRect {
        return CGRect(x: paddLeft, y: paddTop, width: width.cg - paddLeft - paddRight, height: height.cg - paddTop - paddBottom)
    }
    
    
    func getInnerPivot(inspView: InspView<AnyObject>) -> UnitPoint {
        guard let mediaImage = inspView.asInspMediaView()?.media else {return UnitPoint(x: 0.5, y: 0.5)}
        
        return UnitPoint(x: mediaImage.innerPivotX.cg, y: mediaImage.innerPivotY.cg)
    }
    
    private var onInvalidateAction: (() -> Void)? = nil
    
    func onInvalidate(action: (() -> Void)?) {
        onInvalidateAction = action
    }
    
    func invalidate() {
        if let action = onInvalidateAction {
            action()
        }
    }
}

extension ViewPlatform {
    var viewApple: ViewPlatformApple {
        
        return self as! ViewPlatformApple
    }
}
