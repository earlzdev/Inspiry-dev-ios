//
//  VideoTimelineView.swift
//  iosApp
//
//  Created by rst10h on 20.10.22.
//

import UIKit
import AVFoundation

struct VideoTimelineTrim {
    var start:Float64
    var end:Float64
}

class VideoTimelineView: UIView {
    
    public private(set) var asset:AVAsset? = nil
    
    var repeatOn:Bool = false
    
    public private(set) var trimEnabled:Bool = false
    
    
    
    var currentTime:Float64 = 0
    public private(set) var duration:Float64 = 0
    
    let timelineView = TimelineView()
    
    override init (frame: CGRect) {
        super.init(frame: frame)
        timelineView.mainView = self
        timelineView.scroller.frameImagesView.mainView = self
        
        self.addSubview(timelineView)
    }
    
    required init(coder aDecoder: NSCoder) {
        fatalError("MainView init(coder:) has not been implemented")
    }
    
    
    func viewDidLayoutSubviews() {
        coordinate()
    }
    
    func coordinate() {
        timelineView.coordinate()
    }
    
    var url: URL? = nil
    
    func new(url: URL?) {
        guard let url = url else { return }
        self.url = url
        let new = AVAsset(url: url)
        asset = new
        duration = CMTimeGetSeconds(new.duration)
        timelineView.newMovieSet()
        
    }
    
    
    func setTrim(start:Float64, end:Float64, seek:Float64?, animate:Bool) {
        
        var seekTime = currentTime
        if let time = seek {
            seekTime = time
        }
        if animate {
            timelineView.setTrimWithAnimation(trim:VideoTimelineTrim(start:start, end:end), time:seekTime)
        } else {
            timelineView.setTrim(start:start, end:end)
            if seek != nil {
                moveTo(seek!, animate:animate)
            }
        }
    }
    
    func setTrimIsEnabled(_ enabled:Bool) {
        trimEnabled = enabled
        timelineView.setTrimmerStatus(enabled:enabled)
    }
    
    func setTrimmerIsHidden(_ hide:Bool) {
        timelineView.setTrimmerVisible(!hide)
    }
    
    func currentTrim() -> (start:Float64, end:Float64) {
        return (0, 0)
    }
    
    func moveTo(_ time:Float64, animate:Bool) {
        if animate {
            
        } else {
            accurateSeek(time, scrub:false)
            timelineView.setCurrentTime(time, force:true)
        }
    }
    
    //MARK: - seeking
    var previousSeektime:Float64 = 0
    func timelineIsMoved(_ currentTime:Float64, scrub:Bool) {
        
        previousSeektime = currentTime
    }
    
    func accurateSeek(_ currentTime:Float64, scrub:Bool) {
        previousSeektime = currentTime
        timelineIsMoved(currentTime, scrub:scrub)
    }
    
    var playerTimer = Timer()
    
    var reachFlg = false
    @objc func playerTimerAction(_ timer:Timer) {
        
    }
    
    func resizeHeightKeepRatio(_ size:CGSize, height:CGFloat) -> CGSize {
        var result = size
        let ratio = size.width / size.height
        result.height = height
        result.width = height * ratio
        return result
    }
}
