//
//  VideoPlayerApple.swift
//  iosApp
//
//  Created by rst10h on 28.01.22.
//

import Foundation
import shared
import AVFoundation
import UIKit

class VideoPlayerApple: BaseVideoPlayer {
    
    var looper: UIVideoPlayerLooper? = nil
    var name: String? = nil
    var imageAssetGenerator: AVAssetImageGenerator? = nil
    
    func getDuration() -> Int64 {
        if let duration = looper?.videoDuration_ms { return Int64(duration)}
        return -1
    }
    
    func isPlaying() -> Bool {
        return looper?.isPlaying == true
    }
    
    func pause() {
        looper?.stop()
    }
    
    func seekTo(frame: Int) {
        let time = CMTime(seconds: frame.double / FrameConstantsKt.FPS.int.double, preferredTimescale: 1000)
        looper?.seekTo(time: time)
    }
    
    func seekTo(timeMs: Int) {
        let time = CMTime(seconds: timeMs.double / 1000, preferredTimescale: 1000)
        looper?.seekTo(time: time)
    }
    func play() {
        looper?.play()
    }
    
    func setProgressHandler(positionChangeHandler: @escaping (CMTime) -> Void) {
        
        looper?.removePeriodicTimeObserver()
        looper?.addPeriodicTimeObserver(handler: positionChangeHandler)
    }
    
    func removeProgressHandler() {
        looper?.removePeriodicTimeObserver()
    }
    
    func prepare(url: String) {
        let asset = ResourceContainerExtKt.getAssetByFilePath(MR.assets(), filePath: url.removeSheme())
        
        name = asset.originalPath
        
        looper = UIVideoPlayerLooper(url: URL(string: "file://\(asset.path)")!, frame: .zero)
        if let action = onPrepared {
            action()
        }
    }
    
    func prepare(path: URL, videoGravity: AVLayerVideoGravity) {
        looper = UIVideoPlayerLooper(url: path, frame: .zero, videoGravity: videoGravity)
        
        if let action = onPrepared {
            action()
        }
    }
    
    func release() {
        looper?.stop()
        looper = nil
    }
    
    func getFrameImage(timeMs: Int) -> UIImage {
        
        if (imageAssetGenerator == nil) {
            imageAssetGenerator = AVAssetImageGenerator(asset: looper!.videoAsset)
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
        looper?.hasAudio == true
    }
    
    var onError: ((KotlinThrowable) -> Void)? = nil
    
    var onPrepared: (() -> Void)? = nil
    
    

}
