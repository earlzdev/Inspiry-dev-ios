//
//  MusicPlayerViewModel.swift
//  MusicFeatureIos
//
//  Created by vlad on 11/4/21.
//

import Foundation
import Combine
import os
import ModernAVPlayer
import SwiftUI
import shared

class MusicPlayerViewModel: BaseAudioPlayer, ObservableObject {
    
    var errorListener: ((KotlinThrowable) -> Void)?
    
    @Published
    public var isPlaying: Bool
    
    @Published
    public private(set) var selectedTrack: String?
    
    @Published
    public private(set) var totalTime: Int64 = 0
    
    @Published
    public private(set) var currentTime: Int64 = 0
    
    @Published
    public private(set) var isLoading: Bool = false
    
    @Published
    public private(set) var progress: CGFloat = 0
    
    private let backgroundQueue: DispatchQueue = DispatchQueue(label: "loadingTrackBackgroundQueue")
    private var currentTimeCancellable: AnyCancellable?
    private var loadingCancellable: AnyCancellable?
    
    let logger = Logger(subsystem: Bundle.main.bundleIdentifier!, category: "MusicPlayerViewModel")
    let player = ModernAVPlayer()
    
    private let delegate: PlayerDelegate = PlayerDelegate()
    
    
    
    init () {
        isPlaying = false
        selectedTrack = nil
        
        setDelegates()
    }
    
    init (isPlaying: Bool, selectedTrack: String?) {
        self.isPlaying = isPlaying
        self.selectedTrack = selectedTrack
        setDelegates()
    }
    
    func setDelegates() {
        delegate.viewModel = self
        player.delegate = delegate
    }
    
    func setVolume(volume: Float) {
        player.player.volume = volume
    }
    
    func getVolume() -> Float {
        return player.player.volume
    }
    
    func seekTo(progress: Double) {
        let newPositionSeconds = Double(totalTime) / 1000 * progress
        print("seekTo call \(newPositionSeconds), progress \(progress)")
        
        currentTime = Int64(newPositionSeconds * 1000)
        player.seek(position: newPositionSeconds)
    }
    func seekToPositionMs(timeMs: Int64) {
        player.seek(position: Double(timeMs) / 1000.0)
    }
    
    func getProgress() -> CGFloat {
        if totalTime == 0 {
            return 0
        }
        return CGFloat(currentTime) / CGFloat(totalTime)
    }
    
    func playTrack(url: String) {
        selectedTrack = url
        
        isPlaying = true
        totalTime = 0
        currentTime = 0

        player.stop()
        
        guard
            let musicUrl = URL(string: url)
        else {
            print("music resource \(url) is not reachable")
            return
        }
        print("play music \(url)")
        let media = ModernAVPlayerMedia(url: musicUrl, type: MediaType.stream(isLive: true))
        player.load(media: media, autostart: true)
    }
    
    class PlayerDelegate: ModernAVPlayerDelegate {
        weak var viewModel: MusicPlayerViewModel? = nil
        
        func modernAVPlayer(_ player: ModernAVPlayer, didStateChange state: ModernAVPlayer.State) {
            
            print("state \(state) \(player.currentMedia?.url)")
            switch state {
            case ModernAVPlayer.State.playing:
                viewModel?.isPlaying = true
                viewModel?.currentTimeJob()
            
            case ModernAVPlayer.State.paused:
                viewModel?.isPlaying = false
                viewModel?.cancelCurrentTimeJob()
            
            case ModernAVPlayer.State.loading:
                viewModel?.isLoading = true
                
            case ModernAVPlayer.State.loaded:
                viewModel?.isLoading = false
                
                
            case ModernAVPlayer.State.failed:
                viewModel?.cancelCurrentTimeJob()
                
            default:break
                
                
            }
        }
               
        func modernAVPlayer(_ player: ModernAVPlayer, didItemPlayToEndTime endTime: Double) {
            viewModel?.isPlaying = false
            viewModel?.cancelCurrentTimeJob()
        }
        
        func modernAVPlayer(_ player: ModernAVPlayer, didItemDurationChange itemDuration: Double?) {
            
            if itemDuration == nil {
                viewModel?.totalTime = 0
            } else {
                viewModel?.totalTime = Int64(itemDuration! * 1000)
            }
        }
        
        func modernAVPlayer(_ player: ModernAVPlayer, didCurrentTimeChange currentTime: Double) {
            
            viewModel?.currentTime = Int64(currentTime * 1000)
        }
    }
    
    func cancelCurrentTimeJob() {
        currentTimeCancellable?.cancel()
        currentTimeCancellable = nil
    }
    
    func currentTimeJob(assignTimeBeforeStart: Bool = true) {
        
        if assignTimeBeforeStart {
            currentTime = Int64(player.currentTime * 1000)
        }
        
        currentTimeCancellable?.cancel()
        
        currentTimeCancellable = Timer.publish(every: 0.1, on: .main, in: .default)
            .autoconnect()
            .sink(
                receiveValue: { [weak self] value in
                    self?.currentTime = Int64((self?.player.currentTime ?? 0) * 1000)
                }
            )
    }
    
    func play() {
        print("music play action")
        player.play()
    }
    
    func pause() {
        player.pause()
    }
    
    func currentTimeMillis() -> Int64 {
        return currentTime
    }
    
    func getDurationMillis() -> Int64 {
        return totalTime
    }
    
    func isPlayWhenReady() -> Bool {
        return true
    }
    
    func prepare(url: String, startPlayImmediately: Bool, position: Double) { //position in miliseconds here
        selectedTrack = url
        
        isPlaying = startPlayImmediately
        totalTime = 0
        currentTime = 0
        let exists: Bool
        if (url.getSheme().lowercased().starts(with: "file") ) {
            print("music is local file")
            exists = (try? URL(string: url)?.checkResourceIsReachable()) ?? false
        } else {
            exists = true
        }
        guard exists else {
            print("music resource \(url) is not reachable (prepare)")
            return
        }
        print("prepare music \(url) is exists \(exists) position \(position) autoplay \(startPlayImmediately)")
        if let musicURL = URL(string: url) {
            let media = ModernAVPlayerMedia(url: musicURL, type: MediaType.clip)
            self.player.load(media: media, autostart: startPlayImmediately, position: position / 1000.0) //position in seconds here
        } else {
            print("bad music url!!")
        }

    }
    
    func release() {
        print("release music player")
        player.stop()
        player.seek(position: 0)
        selectedTrack = nil
        
    }
    
    func seekTo(timeMillis: Int64) {
        print("seek music to position \(timeMillis)ms")
        seekToPositionMs(timeMs: timeMillis)
    }
    
    func setLoop(enabled: Bool) {
        player.loopMode = enabled
    }
    
}
