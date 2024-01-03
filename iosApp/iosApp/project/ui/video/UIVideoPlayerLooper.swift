//
//  VideoPlayerLoop.swift
//  iosApp
//
//  Created by rst10h on 22.01.22.
//

import AVFoundation
import SwiftUI
import UIKit
import shared

class UIVideoPlayerLooper: UIView {
    
    private let url: URL
    
    private var playerLayer = AVPlayerLayer()
    private var playerLooper: AVPlayerLooper?
    let player: AVQueuePlayer
    
    var videoAsset: AVAsset
    
    var videoSize: CGSize? = nil
    var isPlaying = false
    var videoDuration_ms: Double? = nil
    
    private(set) var hasAudio: Bool = false

    
    required init(url: URL, frame: CGRect, videoGravity: AVLayerVideoGravity = .resizeAspectFill) {
        self.url = url
        
        let playerItem = AVPlayerItem(url: url)
        player = AVQueuePlayer(playerItem: playerItem)
        self.videoAsset =  playerItem.asset
        
        super.init(frame: frame)
        
        playerLayer.player = player
        playerLayer.videoGravity = videoGravity
        layer.addSublayer(playerLayer)
        
        playerLooper = AVPlayerLooper(player: player, templateItem: playerItem)
        

        
        let tracks = videoAsset.tracks(withMediaType: AVMediaType.video)
        hasAudio = !videoAsset.tracks(withMediaType: .audio).isEmpty
        if let videoTrack = tracks.first {
            let size = videoTrack.naturalSize
            let txf = videoTrack.preferredTransform
            let realVidSize = size.applying(txf)
            videoSize = CGSize(width: abs(realVidSize.width), height: abs(realVidSize.height))
            videoDuration_ms = videoAsset.duration.seconds * 1000
        }
    }
    
    var timeObserverToken: Any?

    func addPeriodicTimeObserver( handler: @escaping (CMTime) -> Void ) {
        let timeScale = CMTimeScale(NSEC_PER_SEC)
        let times = CMTime(seconds: 0.03, preferredTimescale: timeScale)

        timeObserverToken = player.addPeriodicTimeObserver(forInterval: times,
                                                          queue: .main) {
            [weak self] time in
                handler(time)
        }
    }
    
    func removePeriodicTimeObserver() {
        if let timeObserverToken = timeObserverToken {
            player.removeTimeObserver(timeObserverToken)
            self.timeObserverToken = nil
        }
    }
    
    func play() {
        isPlaying = true
        player.play()
    }
    
    func stop() {
        isPlaying = false
        player.pause()
    }
    
    func seekTo(time: CMTime) {
        player.seek(to: time, toleranceBefore: CMTime.zero, toleranceAfter: CMTime.zero)
    }
    
    func seekTo(time: CMTime, completionHandler: @escaping (Bool) -> Void) {
        player.seek(to: time, toleranceBefore: CMTime.zero, toleranceAfter: CMTime.zero, completionHandler: completionHandler)
    }
    
//    func getCurrentPixelBuffer() -> CVPixelBuffer? {
//        let s = playerLayer.displayedPixelBuffer()
//    }
    
    func setVolume(volume: Float) {
        player.volume = volume
    }
    
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func layoutSubviews() {
        super.layoutSubviews()
        playerLayer.frame = bounds
    }
}
