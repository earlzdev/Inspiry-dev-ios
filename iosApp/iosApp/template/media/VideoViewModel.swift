//
//  VideoViewModel.swift
//  iosApp
//
//  Created by vlad on 24/1/22.
//

import Foundation
import SwiftUI
import AVFoundation
import shared


class VideoViewModel: ObservableObject {
    
    var player: AVPlayer? = nil
    var playerTimeSubscription: Any? = nil
    
    
    func onAppear(url: URL) {
        
    }
    
    func onDisappear() {
        player?.pause()
    }
    
    func addPlayerTimeSubscription() {
        if playerTimeSubscription == nil {
            self.playerTimeSubscription = self.player?.addPeriodicTimeObserver(forInterval: CMTime(seconds: 0.5, preferredTimescale: 600), queue: .main) { time in
                print("playing video time \(time)")
            }
        }
    }
    
    func removePlayerTimeSubscription() {
        if playerTimeSubscription != nil {
            self.player?.removeTimeObserver(playerTimeSubscription!)
        }
    }
    
    func initErrorSubscription() {
        let errorSubscription = self.player?.observe(\AVPlayer.error, options: .new) { player, change in
            print("AVPlayer error \(String(describing: player.error)), change \(change)")
        }
    }
}
