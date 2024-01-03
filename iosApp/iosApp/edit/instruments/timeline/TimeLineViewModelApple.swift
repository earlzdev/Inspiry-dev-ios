//
//  TimeLineViewModelApple.swift
//  iosApp
//
//  Created by rst10h on 25.08.22.
//

import Foundation
import shared

class TimeLineViewModelApple: TrimVideoModel {
    
    static let templateMaxDurationSec = 120
    
    static let predefinedDurationsSec = [15, 30, 60, 120]
    
    let template: InspTemplateView
    
   
    @Published
    var templateDurationMs: Int64 {
        didSet {
            //self.progress = Float (template.getCurrentTime()) / Float(templateDurationMs)
        }
    }
    
    override var minValue: Float {
        get {
            return 0
        }
        set {}
    }
    
    @Published var durationSec: Float = 0
    
    required init(template: InspTemplateView) {
        self.template = template
        let duration = Int64(template.getDuration_().double * FrameConstantsKt.FRAME_IN_MILLIS)
        self.templateDurationMs = duration
        let max = (duration.float / Float(Self.templateMaxDurationSec * 1000))
        //let progress = Float (template.getCurrentTime()) / Float(duration)
        super.init(url: nil, minValue: 0, maxValue: max, progress: nil)
    }
    
    override func onTrimRight(newValue: Float) {
        maxValue = newValue
        let newDurationMs = (Double(Self.templateMaxDurationSec) * newValue.double) * 1000
        templateDurationMs = Int64(newDurationMs)
        let newDurationFrames = Int32(newDurationMs / FrameConstantsKt.FRAME_IN_MILLIS)
        //self.progress = Float (template.getCurrentTime()) / Float(Self.templateMaxDurationSec * 1000)
        template.setNewDuration(newDuration: newDurationFrames)
    }
    
    override func setNewRangeForMove(left: Float, right: Float) {
        //nothing for moving
    }
    
}
