//
//  ScalableTrimVideoModel.swift
//  iosApp
//
//  Created by rst10h on 14.10.22.
//

import Foundation
import AVFoundation
import UIKit
import shared

class ScalableTrimSliderModel: ObservableObject {
    
    @Published
    var url: URL? = nil {
        didSet {
            self.videoDuration = url?.videoDurationSeconds() ?? 0
        }
    }
    
    @Published
    var progress: Float? = nil
    
    @Published
    var currentScale: CGFloat = 1 {
        didSet {
            self.cachedLeadingPosition = -leadingKnobPosition
            self.maxContentWidth = self.frameWidth * currentScale
        }
    }

    @Published
    var isPlaying: Bool = false
    
    @Published
    var localLeft: Float = 0
    
    @Published
    var localRight: Float = 1
    
    @Published
    var leadingKnobPosition: CGFloat = 0 { didSet {
        self.leftShadingSize = ScalableTrimSlider.horizontalSpacing - self.leadingKnobPosition
        self.updateLeadSpacingSize()
    }
    }
    
    var cachedLeadingPosition: CGFloat = 0
    
    @Published
    var trailingKnobPosition: CGFloat = 0 { didSet {
        self.rightShadingSize = ScalableTrimSlider.horizontalSpacing + self.trailingKnobPosition
        self.updateTrailSpacingSize()
    }}
    
    @Published
    var leftShadingSize = ScalableTrimSlider.horizontalSpacing
    
    @Published
    var rightShadingSize = ScalableTrimSlider.horizontalSpacing
    
    @Published
    var leadShadingOffset = 0.cg
    
    @Published
    var trailShadingOffset = 0.cg
    
    @Published
    var currentGesutre: DragArea? = nil
    
    @Published
    var centerPosition: Float = 0
    
    var thumbnailsCache: [Int: UIImage] = [:]
    
    var timer: Timer? = nil
    
    var minSelectionWidth: CGFloat = 10
    
    var isMinWidth: Bool = false
    
    var autoscroll: ((_ speed: CGFloat, _ maxOver: CGFloat) -> Void)? = nil
    
    var scrollTo: ((_ value: Double) -> Void)? = nil
    
    @Published
    var trimmedDurationMs: Double = 0
    
    var frameWidth = UIScreen.screenWidth - ScalableTrimSlider.horizontalSpacing * 2
    
    var maxContentWidth: CGFloat = 0
    
    
    
    private var videoDuration: Float64 = 0 {
        didSet {
        }
    }
    
    init(url: URL?) {
        self.url = url
    }
    
    init(url: URL?, minValue: Float, maxValue: Float, progress: Float?) {
        self.url = url
        self.progress = progress
        
    }
    
    private func mayScrollLaunch(isRight: Bool) {
        guard timer == nil else { return }
        timer = Timer.scheduledTimer(timeInterval: 0.01, target: self, selector: #selector(self.scrollAnimation(_:)), userInfo: nil, repeats: true)
        RunLoop.main.add(timer!, forMode:RunLoop.Mode.common)
    }
    
    @objc func scrollAnimation(_ timer:Timer) {
        if (currentGesutre == nil) {
            timer.invalidate()
            self.timer = nil
            return
        }
        if (self.currentGesutre == .trailing) {
            if trailingKnobPosition < 0 || trailingKnobPosition > 100 {
                var value = trailingKnobPosition
                if (trailingKnobPosition > 100 && leadingKnobPosition > 0) {
                    value = trailingKnobPosition - 100
                    let min = getMinPossibleWidth()
                    let width = -trailingKnobPosition + frameWidth + leadingKnobPosition - value * 0.5
                    if (width < min) { value = 0}
                    
                }
                if (value > 0 && leadingKnobPosition <= 0) {}
                else {
                    
                    autoscroll!(-value / 100, value )
                }
            }
        }
        if (self.currentGesutre == .leading) {
            if (leadingKnobPosition > 0 || leadingKnobPosition < -100) {
                var value = leadingKnobPosition
                if (leadingKnobPosition < -100) {
                    value = leadingKnobPosition + 100
                    let min = getMinPossibleWidth()
                    let width = -trailingKnobPosition + frameWidth + leadingKnobPosition - value * 0.5
                    if (width < min) { value = 0}
                }
                if (value < 0 && trailingKnobPosition >= 0) {}
                else {
                    autoscroll!(-value / 100, value )
                }
            }
        }
    }
    
    func getCurrentTimeString() -> String {
        let duration = self.trimmedDurationMs
        let string = TrackUtils.init().convertTimeToString(durationMs: Int64(duration))
        if (duration < 2000) {
            let result = "\(string).\(String(format: "%02d", Int((duration / 10).truncatingRemainder(dividingBy: 100))))"
            return result.substring(from: 1)
        }
        return string
    }

    func setRange(left: Double, right: Double) {
        let range = right - left
        self.currentScale = 1 / range
        self.startOffset = frameWidth * currentScale * left
        self.realOffset = startOffset
        print("setup startoffset frameWidth \(frameWidth) scale \(currentScale) left \(left)")
    }
    
    func getMinPossibleWidth() -> CGFloat {
        let minWidth = max(0.05 * (frameWidth * currentScale) / videoDuration, 70)
        print("minimum width = \(minWidth)")
        return minWidth
    }
    
    func dragLeading(dx: CGFloat, frameWidth: CGFloat) {
        let relativePositionLeft = ScalableTrimSlider.horizontalSpacing - leadingKnobPosition - ScalableTrimSlider.thumbnailsPadding - dx
        guard (relativePositionLeft > 3 ) else { return }
        let width = -trailingKnobPosition + frameWidth + leadingKnobPosition - dx
        print("may minimum leading \(width) dx \(dx)")
        let min = getMinPossibleWidth()
        if (width < min && dx < 0) {
            return
        }
        leadingKnobPosition += dx
        if (leadingKnobPosition != 0) {
            mayScrollLaunch(isRight: false)
            onTrimLeft(newValue: getLeftValue())
        }
        
    }
    
    func dragTrailing(dx: CGFloat, frameWidth: CGFloat) {
        let relativePositionRight = ScalableTrimSlider.horizontalSpacing + trailingKnobPosition - ScalableTrimSlider.thumbnailsPadding + dx
        guard (relativePositionRight > 3 ) else { return }
        let width = -trailingKnobPosition + frameWidth + leadingKnobPosition - dx
        print("may minimum trailing \(width) dx \(dx)")
        let min = getMinPossibleWidth()
        if (width < min && dx > 0) {
            return
        }
        trailingKnobPosition += dx
        if (trailingKnobPosition != 0) {
            mayScrollLaunch(isRight: true)
            onTrimRight(newValue: getRightValue())
        }
    }
    
    func resetKnobs(containerWidth: CGFloat) {
        
        let timeWidth = containerWidth * currentScale
        
        let left = startOffset - leadingKnobPosition
        let right = startOffset + containerWidth - trailingKnobPosition
        
        let newDurationWidth = right - left
        
        currentScale = timeWidth / newDurationWidth
        leadingKnobPosition = 0
        trailingKnobPosition = 0
    }
    
    var startOffset: CGFloat = 0 {
        didSet {
            print("startoffset = \(startOffset) scale = \(currentScale)")
        }
    }
    
    func updateLeadSpacingSize() {
        if realOffset > ScalableTrimSlider.horizontalSpacing {
            self.leadShadingOffset = 0
        } else {
            leadShadingOffset = ScalableTrimSlider.horizontalSpacing - realOffset
        }
    }
    func updateTrailSpacingSize() {
        let max = maxContentWidth - frameWidth - ScalableTrimSlider.horizontalSpacing
        if realOffset < max {
            trailShadingOffset = 0
        } else {
            trailShadingOffset = realOffset - max
        }
    }
    
    func onTrimLeft(newValue: Double) {
        
    }
    
    func onTrimRight(newValue: Double) {
        
    }
    
    func onMove(newLeft: Double, newRight: Double) {
        
    }
    
    func getLeftValue() -> Double {
        var value = ((realOffset - leadingKnobPosition) / (frameWidth * currentScale))
        print("startoffset set left \(value)")
        if (value < 0) { value = 0 }
        return value
    }
    
    func getRightValue() -> Double {
        var value = ((realOffset + frameWidth - trailingKnobPosition) / (frameWidth * currentScale))
        print("startoffset set new right \(value)")
        if (value > 1) { value = 1 }
        return value
    }
    @Published
    var realOffset: CGFloat = 0 {
        didSet {
            updateLeadSpacingSize()
            updateTrailSpacingSize()
        }
    }
    
    var isUserAction: Bool = false
    func onOffsetUpdated(newValue: CGFloat, rawNew: CGFloat, rawOld: CGFloat) {
        guard autoscroll != nil else { return }
        if (currentGesutre == .trailing) {
            
            leadingKnobPosition = rawNew - startOffset
            onTrimRight(newValue: getRightValue())
        }
        if (currentGesutre == .leading) {
            trailingKnobPosition = rawNew - startOffset
            onTrimLeft(newValue: getLeftValue())
            
        }
        
        if (currentGesutre == .center || currentGesutre == nil) {
            startOffset = rawNew
            if (isUserAction) {
                onMove(newLeft: getLeftValue(), newRight: getRightValue())
            }
        }
        DispatchQueue.main.async {
            self.realOffset = rawNew
        }
        
        //offsetValue = newValue.float
        print("real offset = \(realOffset) start offset = \(startOffset)")
    }
    
    func removeCallbacks() {
        self.scrollTo = nil
        self.autoscroll = nil
    }
}

extension String {
    func index(from: Int) -> Index {
        return self.index(startIndex, offsetBy: from)
    }

    func substring(from: Int) -> String {
        let fromIndex = index(from: from)
        return String(self[fromIndex...])
    }

    func substring(to: Int) -> String {
        let toIndex = index(from: to)
        return String(self[..<toIndex])
    }

    func substring(with r: Range<Int>) -> String {
        let startIndex = index(from: r.lowerBound)
        let endIndex = index(from: r.upperBound)
        return String(self[startIndex..<endIndex])
    }
}
