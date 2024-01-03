//
//  InspVideoPlayerApple.swift
//  iosApp
//
//  Created by rst10h on 10.10.22.
//

import Foundation
import shared
import AVFoundation
import UIKit
import SwiftUI

/**
 This class allows you to render a video stream into a buffer,
 which will then be displayed on the screen via UIView.layer.
 This method allows you to capture the contents of the view as an image, and is also very fast.
 */
class TemplateVideoPlayer: BaseVideoPlayer {
    
    
    let innerPlayerView = {
        let v = UIView()
        v.layer.contentsGravity = .resizeAspect
        return v
    }()
    
    var player: AVPlayer? = nil
    var name: String? = nil
    var imageAssetGenerator: AVAssetImageGenerator? = nil
    var videoAsset: AVAsset? = nil
    
    private var hasAudio = false
    var videoSize: CGSize? = nil
    private var videoDuration_ms: Double = -1
    
    private var isPlayingState: Bool = false
    
    private var playerItemVideoOutput = AVPlayerItemVideoOutput()
    
    private var displayLink: CADisplayLink? = nil
    
    private var transform: CGAffineTransform? = nil
    
    func getDuration() -> Int64 {
        //        if let duration = looper?.videoDuration_ms { return Int64(duration)}
        //        return -1
        
        return Int64(videoDuration_ms)
        
        //        if let d = videoAsset?.duration.seconds {
        //            return Int64(d) * 1000
        //        } else {
        //            fatalError("get duration from video asset failed \(name)")
        //        }
    }
    
    func pause() {
        isPlayingState = false
        player?.pause()
    }
    
    func seekTo(frame: Int) {
        let time = CMTime(seconds: frame.double / FrameConstantsKt.FPS.int.double, preferredTimescale: 1000)
        player?.seek(to: time, toleranceBefore: CMTime.zero, toleranceAfter: CMTime.zero)
    }
    
    func seekTo(timeMs: Int) {
        let time = CMTime(seconds: timeMs.double / 1000, preferredTimescale: 1000)
        player?.seek(to: time, toleranceBefore: CMTime.zero, toleranceAfter: CMTime.zero)
    }
    
    func seekSyncTo(timeMs: Int, complectionHandler: @escaping () -> Void) {
        if (displayLink != nil) {
            displayLink?.invalidate()
            displayLink = nil
        }
        let time = CMTime(seconds: timeMs.double / 1000, preferredTimescale: 1000)
        player?.seek(to: time, toleranceBefore: CMTime.zero, toleranceAfter: CMTime.zero) {[self] _ in
            let currentTime = playerItemVideoOutput.itemTime(forHostTime: CACurrentMediaTime())
            if playerItemVideoOutput.hasNewPixelBuffer(forItemTime: currentTime) {
                if let pixelBuffer = playerItemVideoOutput.copyPixelBuffer(forItemTime: currentTime, itemTimeForDisplay: nil) {
                    setVideoContent(pixelBuffer: pixelBuffer)
                    
                    complectionHandler()
                }
            }
        }
    }
    
    func play() {
        isPlayingState = true
        player?.play()
    }
    
    func isPlaying() -> Bool {
        return isPlayingState
    }
    
    var timeObserverToken: Any?
    
    private func addPeriodicTimeObserver( handler: @escaping (CMTime) -> Void ) {
        let timeScale = CMTimeScale(NSEC_PER_SEC)
        let times = CMTime(seconds: 0.03, preferredTimescale: timeScale)
        
        timeObserverToken = player?.addPeriodicTimeObserver(forInterval: times,
                                                            queue: .main) {
            [weak self] time in
            handler(time)
        }
    }
    
    private func removePeriodicTimeObserver() {
        if let timeObserverToken = timeObserverToken {
            player?.removeTimeObserver(timeObserverToken)
            self.timeObserverToken = nil
        }
    }
    
    func setProgressHandler(positionChangeHandler: @escaping (CMTime) -> Void) {
        
        removePeriodicTimeObserver()
        addPeriodicTimeObserver(handler: positionChangeHandler)
    }
    
    func removeProgressHandler() {
        removePeriodicTimeObserver()
    }
    
    func prepare(url: String) {
        let asset = ResourceContainerExtKt.getAssetByFilePath(MR.assets(), filePath: url.removeSheme())
        
        name = asset.originalPath
        
        self.prepare(path: URL(string: "file://\(asset.path)")!, videoGravity: .resizeAspectFill)
    }
    
    func prepare(path: URL, videoGravity: AVLayerVideoGravity) {
        
        do { //Enabling audio when device in silent mode
            try AVAudioSession.sharedInstance().setCategory(.playback)
        } catch(let error) {
            print(error.localizedDescription)
        }
        
        let playerItem = AVPlayerItem(url: path)
        playerItem.add(playerItemVideoOutput)
        player = AVPlayer(playerItem: playerItem)
        self.videoAsset =  playerItem.asset
        
        let tracks = videoAsset!.tracks(withMediaType: AVMediaType.video)
        hasAudio = !videoAsset!.tracks(withMediaType: .audio).isEmpty
        if let videoTrack = tracks.first {
            let size = videoTrack.naturalSize
            let txf = videoTrack.preferredTransform
            let realVidSize = size.applying(txf)
            let angle = atan2f(txf.b.float, txf.a.float)
            if (angle != 0) {
                transform = txf.rotated(by: .pi).translatedBy(x: -size.width, y: -size.height)
            }
            videoSize = CGSize(width: abs(realVidSize.width), height: abs(realVidSize.height))
            videoDuration_ms = videoAsset!.duration.seconds * 1000
            
            
        }
        
        displayLink = CADisplayLink(target: self, selector: #selector(displayLinkFired(link:)))
        displayLink?.add(to: .current, forMode: .common)
        
        if let action = onPrepared {
            action()
        }
    }
    
    func release() {
        player?.pause()
        player = nil
        name = nil
        videoAsset = nil
        onError = nil
        onPrepared = nil
    }
    
    func getFrameImage(timeMs: Int) -> UIImage {
        
        if (imageAssetGenerator == nil) {
            imageAssetGenerator = AVAssetImageGenerator(asset: videoAsset!)
            imageAssetGenerator?.requestedTimeToleranceBefore = .zero
            imageAssetGenerator?.requestedTimeToleranceAfter = .zero
            imageAssetGenerator?.appliesPreferredTrackTransform = true
            imageAssetGenerator?.apertureMode = AVAssetImageGenerator.ApertureMode.encodedPixels
        }
        
        let time = CMTime(seconds: timeMs.double / 1000, preferredTimescale: 1000)
        let frameImage: CGImage
        do {
            frameImage = try imageAssetGenerator!.copyCGImage(at: time, actualTime: nil)
        } catch let error {
            print("Error: \(error)")
            fatalError("Error: \(error)")
            
        }
        return UIImage(cgImage: frameImage)
    }
    
    func isAudioAvailable() -> Bool {
        return hasAudio
    }
    
    var onError: ((KotlinThrowable) -> Void)? = nil
    
    var onPrepared: (() -> Void)? = nil
    
    @objc func displayLinkFired(link: CADisplayLink) {
        let currentTime = playerItemVideoOutput.itemTime(forHostTime: CACurrentMediaTime())
        if playerItemVideoOutput.hasNewPixelBuffer(forItemTime: currentTime) {
            if let pixelBuffer = playerItemVideoOutput.copyPixelBuffer(forItemTime: currentTime, itemTimeForDisplay: nil) {
                setVideoContent(pixelBuffer: pixelBuffer)
            }
        }
    }
    
    func setVideoContent(pixelBuffer: CVPixelBuffer) {
        if let transform = transform {
            let coreImageContext: CIContext
            if let metalDevice = MTLCreateSystemDefaultDevice() {
                coreImageContext = CIContext(mtlDevice: metalDevice)
            } else {
                coreImageContext = CIContext(options: nil)
            }
            let ciImage = CIImage(cvPixelBuffer: pixelBuffer).transformed(by: transform)
            let videoImage = coreImageContext.createCGImage(
                ciImage,
                from: CGRect(
                    x: 0,
                    y: 0,
                    width: ciImage.extent.width,
                    height: ciImage.extent.height))
            innerPlayerView.layer.contents = videoImage
        } else {
            innerPlayerView.layer.contents = pixelBuffer
        }
    }
    
    
    
}
