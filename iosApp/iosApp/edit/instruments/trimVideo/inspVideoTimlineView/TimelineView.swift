//
//  TimelineView.swift
//  iosApp
//
//  Created by rst10h on 21.10.22.
//

import Foundation
import UIKit
import AVFoundation

class TimelineView: UIView, UIScrollViewDelegate {
    var mainView:VideoTimelineView? = nil
    let scroller = TimelineScroller()
    
    let viewForAnimate = UIScrollView()
    
    let durationPerHeight: Float64? = nil
    var durationSecond: Float64 = 0
    var animating = false
    
    var videoScale = 1.cg
    
    var onOffsetUpdated: ((_ newOffset: CGFloat, _ rawNew: CGFloat, _ rawOld: CGFloat) -> Void)? = nil
    
    override init (frame: CGRect) {
        
        super.init(frame: frame)
        
        self.backgroundColor = UIColor(hue: 0, saturation:0, brightness:0.0, alpha: 0.05)
        
        viewForAnimate.frame.origin = CGPoint.zero
        viewForAnimate.isScrollEnabled = false
        viewForAnimate.isUserInteractionEnabled = false
        self.addSubview(viewForAnimate)
        
        self.addSubview(scroller)
        scroller.delegate = self
        scroller.configure(parent: self)
        coordinate()
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("TimelineView init(coder:) has not been implemented")
    }
    
    
    //MARK: - coordinate
    func coordinate() {
        if mainView == nil {
            print("triview mainView is nil")
            return
        }
        frame = mainView!.bounds
        viewForAnimate.frame.size = self.frame.size
        
        scroller.frame = self.bounds
        scroller.frameImagesView.frame.size.height = scroller.frame.size.height
        scroller.coordinate()
        
        guard let view = mainView else { return }
        guard let _ = view.asset else { return }
        
        if scroller.frameImagesView.frame.size.width <= 0 {
            return
        }
        
        let previousThumbSize = scroller.frameImagesView.thumbnailFrameSize
        scroller.frameImagesView.setThumnailFrameSize()
        
        let defineMin = scroller.frame.size.width - TimelineScroller.HorizontalSpacing * 2
        
        
        print("trimview current width \(defineMin) max = \(defineMin * videoScale) content = \(defineMin * videoScale)")
        scroller.frameImagesView.maxWidth = defineMin// * videoScale
        scroller.setContentWidth(defineMin * videoScale)
        
        scroller.reset()
        
        if view.currentTime <= view.duration {
            setCurrentTime(view.currentTime, force:false)
        }
    }
    
    func snapWidth(_ width:CGFloat, max:CGFloat) -> CGFloat {
        let n = log2((2 * max) / width)
        var intN = CGFloat(Int(n))
        if n - intN >= 0.5 {
            intN += 1
        }
        let result = (2 * max) / (pow(2,intN))
        return result
    }
    
    func scrollPoint() -> CGFloat {
        return scroller.contentOffset.x / scroller.frameImagesView.frame.size.width
    }
    
    
    
    //MARK: new movie set
    func newMovieSet() {
        
        coordinate()
        if let asset = mainView!.asset{
            scroller.frameImagesView.setThumnailFrameSize()
            
            let duration = asset.duration
            durationSecond = CMTimeGetSeconds(duration)
            
            
            let detailThumbSize = scroller.frameImagesView.thumbnailFrameSize
            
            
            let defineMin = scroller.frame.size.width - TimelineScroller.HorizontalSpacing * 2
            
            scroller.frameImagesView.maxWidth = defineMin// * videoScale
            
            scroller.frameImagesView.minWidth = defineMin * videoScale
            
            print("trimview set min max current width min \(defineMin * videoScale) max \(defineMin)")
            
            scroller.setContentWidth(scroller.frameImagesView.minWidth)
            
            scroller.reset()
        }
    }
    
    //MARK: - currentTime
    func setCurrentTime(_ currentTime:Float64, force:Bool) {
        if inAction() && force == false {
            return
        }
        if mainView!.asset == nil {
            return
        }
        print("trimview set current time: \(currentTime)")
        var scrollPoint:CGFloat = 0
        scrollPoint = CGFloat(currentTime / mainView!.duration)
        
        scroller.ignoreScrollViewDidScroll = true
        //todo setup scroll here
        
        scroller.frameImagesView.requestVisible(depth:0, wide:0, direction:0)
        
        scroller.frameImagesView.displayFrames()
    }
    
    func moved(_ currentTime:Float64) {
        mainView!.timelineIsMoved(currentTime, scrub:true)
    }
    
    
    //MARK: - TrimViews
    func setTrimmerStatus(enabled:Bool) {
        
    }
    
    func setTrimmerVisible(_ visible:Bool) {
        
    }
    
    
    func setTrim(start:Float64?, end:Float64?) {
        
    }
    
    func setTrimWithAnimation(trim:VideoTimelineTrim, time:Float64) {
        
    }
    
    
    var manualScrolledAfterEnd = false
    func setTrimViewInteraction(_ active:Bool) {
        if mainView!.trimEnabled == false && active {
            return
        }
        
        
        
        if active {
            setManualScrolledAfterEnd()
        }
    }
    
    func setManualScrolledAfterEnd() {
        
    }
    
    func swapTrimKnobs() {
        
    }
    
    //MARK: - animation
    func startAnimation() {
        scroller.frameImagesView.startAnimation()
    }
    
    func stopAnimation() {
        scroller.frameImagesView.stopAnimation()
    }
    
    
    
    
    
    //MARK: - Gestures
    func inAction() -> Bool {
        if allTouches.count > 0 || scroller.isTracking || scroller.isDecelerating {
            return true
        } else {
            return false
        }
    }
    
    //MARK: - Scrolling
    
    var allTouches = [UITouch]()
    var pinching:Bool = false
    
    func scrollViewDidScroll(_ scrollView:UIScrollView) {
        if scroller.ignoreScrollViewDidScroll {
            scroller.ignoreScrollViewDidScroll = false
            return
        }
        scroller.frameImagesView.displayFrames()
        setTrimViewInteraction(false)
        let scrollPoint = scroller.contentOffset.x / scroller.frameImagesView.frame.size.width
        
        guard let mView = (mainView) else { return }

    }
    
    func scrollViewDidEndDecelerating(_ scrollView: UIScrollView) {
        scroller.frameImagesView.requestVisible(depth:0, wide:0, direction:0)
        setTrimViewInteraction(true)
        let scrollPoint = scroller.contentOffset.x / scroller.frameImagesView.frame.size.width
    }
    
    func scrollViewDidEndDragging(_ scrollView: UIScrollView,
                                  willDecelerate decelerate: Bool) {
        scroller.frameImagesView.requestVisible(depth:0, wide:0, direction:0)
        if decelerate == false {
            setTrimViewInteraction(true)
        }
        let scrollPoint = scroller.contentOffset.x / scroller.frameImagesView.frame.size.width
    }
    
    
    
    //MARK: - Zooming
    
    var pinchCenterInContent:CGFloat = 0
    var pinchStartDistance:CGFloat = 0
    var pinchStartContent:(x:CGFloat,width:CGFloat) = (0,0)
    func pinchCenter(_ pointA:CGPoint, pointB:CGPoint) -> CGPoint {
        return CGPoint(x: (pointA.x + pointB.x) / 2, y: (pointA.y + pointB.y) / 2)
    }
    func pinchDistance(_ pointA:CGPoint, pointB:CGPoint) -> CGFloat {
        return sqrt(pow((pointA.x - pointB.x),2) + pow((pointA.y - pointB.y),2));
    }
    func startPinch() {
        pinching = true
        scroller.isScrollEnabled = false
        
        let touch1 = allTouches[0]
        let touch2 = allTouches[1]
        let center = pinchCenter(touch1.location(in: self),pointB: touch2.location(in: self))
        
        pinchStartDistance = pinchDistance(touch1.location(in: self),pointB: touch2.location(in: self))
        let framewidth = scroller.frame.size.width
        pinchStartContent = ((framewidth / 2) - scroller.contentOffset.x,scroller.contentSize.width - framewidth)
        pinchCenterInContent = (center.x - pinchStartContent.x) / pinchStartContent.width
    }
    
    func updatePinch() {
        let touch1 = allTouches[0]
        let touch2 = allTouches[1]
        let center = pinchCenter(touch1.location(in: self), pointB:touch2.location(in: self))
        var sizeChange = (1 * pinchDistance(touch1.location(in: self), pointB: touch2.location(in: self))) / pinchStartDistance
        
        var contentWidth = pinchStartContent.width * sizeChange
        
        let sizeMin = scroller.frameImagesView.minWidth
        let sizeMax = scroller.frameImagesView.maxWidth
        
        if contentWidth < sizeMin {
            let sizeUnit = sizeMin / pinchStartContent.width
            sizeChange = ((pow(sizeChange/sizeUnit,2)/4) + 0.75) * sizeUnit
            contentWidth = pinchStartContent.width * sizeChange
            contentWidth = sizeMin
        } else if contentWidth > sizeMax {
            sizeChange = sizeMax
            contentWidth = sizeMax
        } else {
            let startRatio = pinchStartContent.width / sizeMax
            let currentRatio = startRatio * sizeChange
            let effect = ((sin(CGFloat.pi * 2 * log2(2/currentRatio)) * 0.108) - (sin(CGFloat.pi * 6 * log2(2/currentRatio)) * 0.009)) * currentRatio
            let resultWidth = sizeMax * (currentRatio + effect)
            contentWidth = resultWidth
        }
        let contentOrigin = center.x - (contentWidth * pinchCenterInContent)
        scroller.contentOffset.x = (scroller.frame.size.width / 2) - contentOrigin
        scroller.setContentWidth(contentWidth)
        scroller.frameImagesView.layout()
        
        scroller.frameImagesView.requestVisible(depth:2, wide:0, direction:0)
    }
    
    func endPinch() {
        scroller.frameImagesView.requestVisible(depth:0, wide:1, direction:0)
        
        let width = snapWidth(scroller.frameImagesView.frame.size.width, max:scroller.frameImagesView.maxWidth)
        
        let offset = self.resizedPositionWithKeepOrigin(width:scroller.frameImagesView.frame.size.width, origin:scroller.contentOffset.x, destinationWidth:width)
        
        UIView.animate(withDuration: 0.1,delay:Double(0.0),options:UIView.AnimationOptions.curveEaseOut, animations: { () -> Void in
            
            self.scroller.setContentWidth(width, setOrigin:false)
            self.scroller.contentOffset.x = offset
            self.scroller.frameImagesView.layout()
        },completion: { finished in
            self.pinching = false
            self.scroller.isScrollEnabled = true
        })
        
        self.scroller.frameImagesView.updateTolerance()
        
        setTrimViewInteraction(true)
        
        guard let mView = (mainView) else { return }

    }
    
    func resizedPositionWithKeepOrigin(width:CGFloat, origin:CGFloat, destinationWidth:CGFloat) -> CGFloat {
        let originPoint = origin / width
        let result = originPoint * destinationWidth
        return result
    }
    
    
}













