//
//  HolderURL.swift
//  iosApp
//
//  Created by rst10h on 28.07.22.
//

import Foundation
import UIKit

class TrimVideoModel: ObservableObject {
   
    @Published
    var url: URL? = nil
    
    @Published
    var minValue: Float = 0
    
    @Published
    var maxValue: Float = 0
    
    @Published
    var progress: Float? = nil

    @Published
    var videoCGWidth: CGFloat = 0
    
    @Published
    var offScreenSize: CGFloat = 0
    
    @Published
    var previewOffset: CGFloat = 0 {
        didSet {
            print("preview value = \(previewOffset)")
        }
    }
    
    @Published
    var offsetValue: Float = 0 { //  < (1 - screenWidth / thumbnailsWidth)
        didSet {
            print("offset value = \(offsetValue)")
        }
    }
    
    init(url: URL?) {
        self.url = url
    }
    
    init(url: URL?, minValue: Float, maxValue: Float, progress: Float?) {
        self.url = url
        self.minValue = minValue
        self.maxValue = maxValue
        self.progress = progress
        
        initThumbnailsVideo()


        
    }
    
    func initThumbnailsVideo() {
        let videoDurationMS = (url?.videoDurationSeconds() ?? 1) * 1000
        let screenSize = UIScreen.screenWidth //todo move this
        if (videoDurationMS > Constants.VISIBLE_VIDEO_DURATION_SIZE_MS.double) {
            let offscreenPercent =  videoDurationMS / Constants.VISIBLE_VIDEO_DURATION_SIZE_MS.double
            self.videoCGWidth = offscreenPercent * screenSize
            self.offScreenSize = videoCGWidth - screenSize
            let midValue = (minValue + maxValue) / 2 //trimed video center
            let visiblePart = (screenSize / videoCGWidth).float
            let initialOffset = midValue - visiblePart / 2
            let maxoffset = 1 - visiblePart

            if (initialOffset < 0) {
                offsetValue = 0
            } else
            if (initialOffset > maxoffset) {
                offsetValue = maxoffset
            } else {
                offsetValue = initialOffset
            }
                
            minValue = minValue - offsetValue
            maxValue = maxValue - offsetValue
            previewOffset = -videoCGWidth * offsetValue.cg
            
        } else {
            videoCGWidth = screenSize
        }
    }
    
    private var isMaxRight = false
    private var isMaxLeft = false
    
    func updateMinValue(dragWidth: Float, frameWidth: Float) {
        
        let nextValue = dragWidth / frameWidth
        guard !isMaxDuration(nextMin: nextValue, nextMax: maxValue) else {
            isMaxLeft = true
            isMaxRight = false
            return
            
        }
        isMaxLeft = false
        minValue = nextValue// + offsetValue
        onTrimLeft(newValue: minValue)
    }
    
    func updateMaxValue(dragWidth: Float, frameWidth: Float) {
        let nextValue = dragWidth / frameWidth
        guard !isMaxDuration(nextMin: minValue, nextMax: nextValue) else { return }
        maxValue = nextValue// + offsetValue
        onTrimRight(newValue: maxValue)
    }
    
    func isMaxDuration(nextMin: Float, nextMax: Float) -> Bool {
        return false
    }
    
    /// override this func for receive new start value
    func onTrimLeft(newValue: Float) {

    }
    
    /// override this func for receive new end value
    func onTrimRight(newValue: Float) {

    }
    
    func onScrollThumbnails(newValue: Float) {
        
    }
    
    func setNewOffset(rawValueDx: CGFloat) {
        var newPreviewOffset = previewOffset - rawValueDx
        if (newPreviewOffset > 0) { newPreviewOffset = 0}
        if (newPreviewOffset < -offScreenSize) { newPreviewOffset = -offScreenSize }
        self.previewOffset = newPreviewOffset
        self.offsetValue = -previewOffset.float / self.videoCGWidth.float
        //onMove(newLeft: minValue + offsetValue, newRight: minValue + offsetValue)
        //todo change left and right bounds of video
        onScrollThumbnails(newValue: self.offsetValue)
    }
    
    func onMove(newLeft: Float, newRight: Float) {
        onTrimRight(newValue: newRight)
        onTrimLeft(newValue: newLeft)
    }
    
    func setNewRangeForMove(left: Float, right: Float) {
        minValue = left
        maxValue = right
        onMove(newLeft: left, newRight: right)
    }
}
