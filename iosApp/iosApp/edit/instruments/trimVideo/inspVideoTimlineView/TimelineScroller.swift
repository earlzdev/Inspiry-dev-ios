//
//  TimelineScroller.swift
//  iosApp
//
//  Created by rst10h on 20.10.22.
//

import UIKit

class TimelineScroller: UIScrollView {
    
    
    
    static let HorizontalSpacing = 50.cg
    
    var parentView:TimelineView? = nil
    let frameImagesView = FrameImagesView()
    
    
    override init (frame: CGRect) {
        super.init(frame: frame)
        
        self.isScrollEnabled = true
        self.isDirectionalLockEnabled = true
        self.showsHorizontalScrollIndicator = false
        self.showsVerticalScrollIndicator = false
        self.bounces = false
        self.decelerationRate = .fast
        self.isMultipleTouchEnabled = true
        self.delaysContentTouches = false
        self.pinchGestureRecognizer?.velocity
        self.frameImagesView.parentScroller = self
        self.addSubview(frameImagesView)
        
        
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("TimelineScroller init(coder:) has not been implemented")
    }
    
    func configure(parent:TimelineView) {
        parentView = parent
        //        trimView.configure(parent, scroller:self)
    }
    
    func reset() {
        frameImagesView.reset()
    }
    func stop() {
        frameImagesView.stop()
    }
    
    var ignoreScrollViewDidScroll:Bool = false
    func setContentWidth(_ width:CGFloat) {
        setContentWidth(width, setOrigin:true)
    }
    
    override var contentOffset: CGPoint {
        get {
            return super.contentOffset
        }
        set {
            let offsetValue = newValue.x / (frameImagesView.frame.width - Self.HorizontalSpacing * 2)
            parentView?.onOffsetUpdated?(offsetValue, newValue.x, super.contentOffset.x)
            super.contentOffset = newValue
        }
    }
    
    
    
    func setContentWidth(_ width: CGFloat, setOrigin:Bool) {
        ignoreScrollViewDidScroll = true
        self.contentSize = CGSize(width:width + Self.HorizontalSpacing * 2, height:self.frame.size.height) //content padding setup here // was: + frame.size.width
        frameImagesView.frame.size.width = width
        
        if setOrigin {
            frameImagesView.frame.origin.x = Self.HorizontalSpacing
        }
        frameImagesView.displayFrames()
    }
    
    var measureHeight:CGFloat = 0
    func coordinate() {
        
        let wholeHeight = self.frame.size.height
        measureHeight = 0//wholeHeight * 0.2
        
        frameImagesView.frame.size.height = wholeHeight// - measureHeight
        if frameImagesView.animating == false {
            frameImagesView.frame.origin = CGPoint(x: 0,y: 0)
        }
        
    }
    
    func visibleRect() -> CGRect {
        var visibleRect = frame
        visibleRect.origin = contentOffset
        if contentSize.width < frame.size.width {
            visibleRect.size.width = contentSize.width
        }
        if contentSize.height < frame.size.height {
            visibleRect.size.height = contentSize.height
        }
        if zoomScale != 1 {
            let theScale = 1.0 / zoomScale;
            visibleRect.origin.x *= theScale;
            visibleRect.origin.y *= theScale;
            visibleRect.size.width *= theScale;
            visibleRect.size.height *= theScale;
        }
        return visibleRect
    }
    
    
    //MARK: - scroll
    func setScrollValue(_ scrollValue: CGFloat, maxOver: CGFloat = 0) {
        
        var nextOffset = self.contentOffset.x + scrollValue * 50
        
        let maxOffset = frameImagesView.frame.size.width - (frameImagesView.frame.size.width / parentView!.videoScale) + maxOver
        let minOffset = maxOver
        
        print("nextOffset \(nextOffset) min \(minOffset) max \(maxOffset) \(scrollValue)")
        if (scrollValue > 0 && nextOffset > maxOffset) {nextOffset = maxOffset}
        if (scrollValue < 0 && nextOffset < minOffset) {nextOffset = minOffset}
        
        
        self.contentOffset = CGPoint(x: nextOffset, y: self.contentOffset.y)
        
        frameImagesView.requestVisible(depth:0, wide:0, direction:0)
        
        frameImagesView.displayFrames()
    }
    
    func setScroll(point: Double) { // point 0..1
        let width = frameImagesView.frame.size.width
        self.contentOffset = CGPoint(x: width * point, y: self.contentOffset.y)
    }
    
    
    //MARK: - Touch Events
    var allTouches = [UITouch]()
    
    override open func touchesBegan(_ touches: Set<UITouch>, with event: UIEvent?)
    {
        
        for touch in touches {
            if !allTouches.contains(touch) {
                allTouches += [touch]
            }
            if !parentView!.allTouches.contains(touch) {
                parentView!.allTouches += [touch]
            }
        }
        
    }
    
    override open func touchesMoved(_ touches: Set<UITouch>, with event: UIEvent?)
    {
        if parentView!.allTouches.count == 2 {
            if parentView!.pinching {
                parentView!.updatePinch()
            } else {
                parentView!.startPinch()
            }
        }
    }
    
    override open func touchesEnded(_ touches: Set<UITouch>, with event: UIEvent?)
    {
        for touch in touches {
            if let index = allTouches.firstIndex(of:touch) {
                allTouches.remove(at: index)
            }
            if let index = parentView!.allTouches.firstIndex(of:touch) {
                parentView!.allTouches.remove(at: index)
            }
        }
        if parentView!.pinching && parentView!.allTouches.count < 2 {
            parentView!.endPinch()
        }
    }
    
    override open func touchesCancelled(_ touches: Set<UITouch>, with event: UIEvent?)
    {
        for touch in touches {
            if let index = allTouches.firstIndex(of:touch) {
                allTouches.remove(at: index)
            }
            if let index = parentView!.allTouches.firstIndex(of:touch) {
                parentView!.allTouches.remove(at: index)
            }
        }
        
        if parentView!.pinching {
            parentView!.endPinch()
        }
    }
}

