//
//  EditWrapperHelperApple.swift
//  iosApp
//
//  Created by rst10h on 30.06.22.
//

import Foundation
import shared
import SwiftUI

class EditWrapperHelperApple: ObservableObject {
    let coreHelper: EditWrapperHelper
    
    @Published
    var editBounds: CGRect = .zero {
        didSet {
            print("wrapperhelper set bounds \(editBounds)")
        }
    }
    
    @Published
    var availableActions: [TouchAction] = []
    
    @Published
    var rotation: Float = 0
    
    @Published
    var isText: Bool = false
    
    @Published
    var isVisible: Bool = false {
        didSet {
            print("wrapperhelper set visible \(isVisible)")
        }
    }
    
    init(helper: EditWrapperHelper) {
        self.coreHelper = helper
        
        CoroutineUtil.watch(state: coreHelper.isWrapperVisible, onValueReceived: {
            [weak self] in self?.isVisible = $0
        })
        
        CoroutineUtil.watch(state: coreHelper.availableActions, onValueReceived: {
            [weak self] in self?.availableActions = $0
        })
        
        CoroutineUtil.watch(state: coreHelper.boundsRotation, onValueReceived: {
            [weak self] in self?.rotation = $0
        })
        CoroutineUtil.watch(state: coreHelper.templateView.currentSize, allowNilState: false, onValueReceived: {
            [weak self] in self?.sizeChanged(size: $0)
        })
        
        CoroutineUtil.onReceived(state: coreHelper.editBounds, onValueReceived: {
            [weak self] in
            self?.updateBounds(rect: self?.coreHelper.templateView.selectedView?.getViewBoundsForWrapper() ?? .zero)
        }
                            )
        coreHelper.onSelectedViewChanged { new, old in
            if let new = new {
                    self.isVisible = (self.coreHelper.isWrapperVisible.value as? KotlinBoolean)?.boolValue ?? false //bacause coroutine for isVisible may be running later
                    self.isText = new.asInspTextView() != nil
                    self.rotation = new.getAbsoluteRotation()
                    let newBounds = new.getViewBoundsForWrapper()
                    self.updateBounds(rect: newBounds)
            }
        }
    }
    
    private func sizeChanged(size: Size) {
        if let view = coreHelper.templateView.selectedView {
            if (coreHelper.selectedView == view) {
                self.updateBounds(rect: view.getViewBoundsForWrapper())
            }
        }
    }
    
    private var rotationStartAngle: CGFloat = 0
    private var rotationCenterPoint: CGPoint = .zero
    private var rotationFirstVector: CGVector = .zero
    
    func startRotation(touchPoint: CGPoint) {
        rotationStartAngle = rotation.cg
        rotationCenterPoint = CGPoint(x: editBounds.midX, y: editBounds.midY)
        rotationFirstVector = CGVector(dx: touchPoint.x - rotationCenterPoint.x, dy: touchPoint.y - rotationCenterPoint.y)

    }
  
    func rotationAction(touchPoint: CGPoint) {
        
        if (rotationFirstVector == .zero) {
            
            startRotation(touchPoint: CGPoint(x: editBounds.minX, y: editBounds.maxY).rotation(Angle(degrees: rotation.double), arround: CGPoint(x: editBounds.midX, y: editBounds.midY)))
        }
        
        let newVector = CGVector(dx: rotationFirstVector.dx + touchPoint.x, dy: touchPoint.y + rotationFirstVector.dy)
        let angle = rotationFirstVector.angleBetween(newVector)
        coreHelper.rotateSelectedView(startAngle: rotationStartAngle.float, deltaAngle: Float(angle.degrees))
        coreHelper.templateView.objectWillChanged()
    }
    
    func endRotation() {
        rotationStartAngle = 0
        rotationFirstVector = .zero
    }
    
    private var isScaleProgress = false
    func scaleAction(translate: CGSize) {

        let touchPoint = PointF(x: translate.width.float, y: translate.height.float)
        
        if (!isScaleProgress) {
            isScaleProgress = true

            coreHelper.startScaleAction(touchPoint: touchPoint)
        } else {
            coreHelper.scaleAction(touchPoint: touchPoint)
            let tv = coreHelper.templateView as! InspTemplateViewApple
            tv.updateLayouts() //todo wrong logic
            (tv.selectedView?.asInspTextView()?.textView as? InnerTextHolderApple)?.layoutUpdate()
            tv.objectWillChanged()
        }
    }
    func endScale() {
        isScaleProgress = false
        coreHelper.finishScaleAction()
    }
    
    private func updateBounds(rect: CGRect) {
        if (isText) {
            let h = rect.height
            let w = rect.width
            let x = rect.minX
            let y = rect.minY
            
            editBounds = CGRect(origin: CGPoint(x: x, y: y ), size: CGSize(width: w + 20, height: h + 20))
        }
        else {
            editBounds = rect
        }
    }
}

extension Rect {
    var cgRect: CGRect {
        CGRect(x: self.left.cg, y: self.top.cg, width: self.width().cg, height: self.height().cg)
    }
}

extension InspView {
    //todo move to actual/except kmm
    @objc func offsetViewBoundsApple(_ rect: CGRect) -> CGRect {
        guard let appleView = self.view as? ViewPlatformApple else { return .zero }
        let dx = appleView.xOffset
        let dy = appleView.yOffset
        let r = rect.offsetBy(dx: dx, dy: dy)
        if let parentGroup = (parentInsp as? InspGroupView)?.asGeneric() {
            return parentGroup.offsetViewBoundsApple(r)
        } else {
            return r
        }
    }
    
    @objc func getViewBoundsForWrapper() -> CGRect {
        guard let appleView = self.view as? ViewPlatformApple else { return .zero }
        
        var wrapperRect: CGRect
        if let parentview = self.findParentIfDependsOnIt()?.view as? ViewPlatformApple {
            wrapperRect = CGRect(origin: CGPoint(x: 0,y: 0), size: parentview.getCGSize())
        } else {
            wrapperRect = CGRect(x: 0, y: 0, width: appleView.width.cg, height: appleView.height.cg)
        }
        
        return offsetViewBoundsApple(wrapperRect)
        
    }
}
