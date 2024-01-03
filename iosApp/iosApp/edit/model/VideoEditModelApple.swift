//
//  VideoEditModelApple.swift
//  iosApp
//
//  Created by rst10h on 28.07.22.
//

import Foundation
import UIKit
import shared

class VideoEditModelApple: ScalableTrimSliderModel {
    
    static let predefinedDurationsSec = [0.1, 0.5, CalculatedDurations.Template.rawValue, CalculatedDurations.VideoDuration.rawValue]
    
    @Published
    var volume: Float = 1 {
        didSet {
            currentView.media.videoVolume?.setValue(volume)
        }
    }
    
   
    let coreModel: VideoEditViewModel
    
    //@Published
    var currentView: InspMediaView {
        didSet {
            url = URL(string: currentView.media.originalSource ?? "")
        }
    }
    
    var videoDurationMs: Double = 0
    
    init(model: VideoEditViewModel) {
        coreModel = model
        
        
        let view = model.currentView.value as! InspMediaView
        self.currentView = view
        super.init(url: URL(string: view.media.originalSource ?? ""))
        
        CoroutineUtil.watch(state: coreModel.currentView, allowNilState: false) {
            [weak self] in self?.onSelectionChanged(newView: $0)
            
        }
        volume = (currentView.getVideoVolumeConsiderDuplicate()?.value as? KotlinFloat)?.floatValue ?? 0
        
        initDuration()
        initProgressHandler()
    }
    
    func onSelectionChanged(newView: InspMediaView) {
        guard newView.isVideo() else { return }
        url = URL(string: newView.media.originalSource ?? "")
        print("change selection to \(newView.media.id) url \(url)")
        forceStopPlaying()
        if let innerVideo = (currentView.innerMediaView as? InnerMediaViewApple)  { innerVideo.removeProgressHandler() }
        currentView = newView
        volume = (currentView.getVideoVolumeConsiderDuplicate()?.value as? KotlinFloat)?.floatValue ?? 0
        initDuration()
        initProgressHandler()
        super.objectWillChange.send()
        
    }
    
    func initProgressHandler() {
        guard let innerVideo = (currentView.innerMediaView as? InnerMediaViewApple) else { return }
        innerVideo.addVideoProgressHandler { time in
            if (self.isPlaying) {
                let fullProgress = time.seconds * 1000 // self.videoDurationMs
                let startTimeMs = (self.currentView.media.videoStartTimeMs?.value as? KotlinInt)?.intValue ?? 0
                let endTimeMs = (self.currentView.media.videoEndTimeMs?.value as? KotlinInt)?.intValue ?? 1
                let p = (fullProgress - startTimeMs.double) / (endTimeMs.double - startTimeMs.double)
                if (self.isPlaying && p >= 1) {
                    self.progress = 0
                    innerVideo.playVideoIfExists(forcePlay: true)
                    
                } else {
                    if (p >= 0) { // it may be during seek processzh  jm
                        self.progress = Float(p)
                    }
                }
            }
        }
    }
       
    func initDuration() {
        progress = nil
        videoDurationMs = (url?.videoDurationSeconds() ?? 0) * 1000
        var startTimeMs = (currentView.media.videoStartTimeMs?.value as? KotlinInt)?.intValue
        var endTimeMs = (currentView.media.videoEndTimeMs?.value as? KotlinInt)?.intValue
        if (endTimeMs == nil) {
            endTimeMs = currentView.getDurationForTrimmingMillis(maxDuration: false).int
            currentView.media.videoEndTimeMs = CoroutinesUtilKt.createStateFlow(value: KotlinInt(int: Int32(endTimeMs!)))
        }
        if (startTimeMs == nil) {
            startTimeMs = currentView.getDurationForTrimmingMillis(maxDuration: false).int
            currentView.media.videoStartTimeMs = CoroutinesUtilKt.createStateFlow(value: KotlinInt(int: Int32(0)))
        }
        let left = startTimeMs!.double / videoDurationMs
        let right = endTimeMs!.double / videoDurationMs
        updateDurationLabel()
        super.setRange(left: left, right: right)
    }
    func setNewRange(durationSec: Double) {
        var newDurationms = durationSec * 1000
        if (durationSec == CalculatedDurations.Template.rawValue) {
            newDurationms = currentView.templateParent.getInitialDuration().double * FrameConstantsKt.FRAME_IN_MILLIS
        }
        if (durationSec == CalculatedDurations.VideoDuration.rawValue) {
            newDurationms = videoDurationMs
        }
        var startTimeMs = ((currentView.media.videoStartTimeMs?.value as? KotlinInt)?.intValue ?? 0).double
        let maxTimeMs = videoDurationMs
        var endTimeMs = startTimeMs + newDurationms
        if (endTimeMs > maxTimeMs) {
            print("end time = \(endTimeMs) max = \(maxTimeMs)")
            let overTime = endTimeMs - maxTimeMs
            endTimeMs -= overTime
            startTimeMs -= overTime
            if (startTimeMs < 0) { startTimeMs = 0}
        }
        super.scrollTo?(startTimeMs / videoDurationMs)
        currentView.media.videoEndTimeMs = CoroutinesUtilKt.createStateFlow(value: KotlinInt(int: Int32(endTimeMs)))
        currentView.media.videoStartTimeMs = CoroutinesUtilKt.createStateFlow(value: KotlinInt(int: Int32(startTimeMs)))
        initDuration()
        mayUpdateTemplateDuration()
    }
    
    private func updateDurationLabel() {
        let startTimeMs = (currentView.media.videoStartTimeMs?.value as? KotlinInt)?.intValue
        let endTimeMs = (currentView.media.videoEndTimeMs?.value as? KotlinInt)?.intValue
        print("new startms = \(startTimeMs!) new end = \(endTimeMs!) duration = \(endTimeMs! - startTimeMs!)")
        self.trimmedDurationMs = endTimeMs!.double - startTimeMs!.double
    }
       
    override func onTrimLeft(newValue: Double) {
        if (isPlaying) {
            forceStopPlaying()
        }
        self.progress = 0
        let newStart = Int(videoDurationMs * newValue)
        currentView.media.videoStartTimeMs?.setValue(newStart.int32.toKotlinInt)
        print("trim left set \(newStart)")
        (currentView.innerMediaView as? InnerMediaViewApple)?.seekTo(timeMs: newStart)
        mayUpdateTemplateDuration()
        updateDurationLabel()
    }
    
    override func onTrimRight(newValue: Double) {
        if (isPlaying) {
            forceStopPlaying()
        }
        self.progress = 1
        let newEnd = Int(videoDurationMs * newValue)
        currentView.media.videoEndTimeMs!.setValue(newEnd.int32.toKotlinInt)
        (currentView.innerMediaView as? InnerMediaViewApple)?.seekTo(timeMs: newEnd)
        mayUpdateTemplateDuration()
        updateDurationLabel()
    }
    
    override func onMove(newLeft: Double, newRight: Double) {
        if (isPlaying) {
            forceStopPlaying()
        }
        DispatchQueue.main.async {
            self.progress = 0
        }
        let newStart = Int(videoDurationMs * newLeft)
        let newEnd = Int(videoDurationMs * newRight)
        currentView.media.videoStartTimeMs?.setValue(newStart.int32.toKotlinInt)
        print("trim left set moving \(newStart)")
        currentView.media.videoEndTimeMs?.setValue(newEnd.int32.toKotlinInt)
        (currentView.innerMediaView as? InnerMediaViewApple)?.seekTo(timeMs: newStart)
    }
    
    private func mayUpdateTemplateDuration() {
        let videoFinishFrame = currentView.getStartFrameShortCut().double + (currentView.getVideoEndTimeMs() - currentView.getVideoStartTimeMs()).double / FrameConstantsKt.FRAME_IN_MILLIS
        let templateDuration = currentView.templateParent.getDuration_()
        if (videoFinishFrame > templateDuration.double) {
            currentView.templateParent.setNewDuration(newDuration: Int32(videoFinishFrame))
        } else if (videoFinishFrame < templateDuration.double && videoFinishFrame > currentView.templateParent.getInitialDuration().double) {
            currentView.templateParent.setNewDuration(newDuration: Int32(videoFinishFrame))
        } else if (videoFinishFrame < templateDuration.double && videoFinishFrame < currentView.templateParent.getInitialDuration().double) {
            currentView.templateParent.setNewDuration(newDuration: currentView.templateParent.template_.initialDuration?.int32Value ?? 240)
        }
    }
    
    func playPauseAction() {
        guard let innerVideo = (currentView.innerMediaView as? InnerMediaViewApple) else { return }
        if (isPlaying) {
            innerVideo.pauseVideoIfExists()
        } else {
            innerVideo.playVideoIfExists(forcePlay: true)
        }
        isPlaying.toggle()
    }
    
    func onDisappear() {
        forceStopPlaying()
        super.removeCallbacks()
        guard let innerVideo = (currentView.innerMediaView as? InnerMediaViewApple) else { return }
        innerVideo.removeProgressHandler()
    }
    
    func forceStopPlaying() {
        isPlaying = false
        self.progress = nil
        guard let innerVideo = (currentView.innerMediaView as? InnerMediaViewApple) else { return }
        if (innerVideo.mediaView?.templateMode != .preview) {
            print("-------- stop video")
            innerVideo.pauseVideoIfExists()
        }
    }
}

enum CalculatedDurations: Double {
case Template = -1
case VideoDuration = -2
}
