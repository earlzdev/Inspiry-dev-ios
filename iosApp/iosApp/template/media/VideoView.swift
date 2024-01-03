//
//  VideoView.swift
//  iosApp
//
//  Created by vlad on 7/11/21.
//

import SwiftUI
import AVFoundation
import shared

struct VideoView: View {
    let url: URL
    let player: AVPlayer
    init(url: URL) {
        self.url = url
        self.player = AVPlayer(url: url)
        self.player.observe(\AVPlayer.error, options: .new) { player, change in
            print("AVPlayer error \(String(describing: player.error)), change \(change)")
        }
    }
    
    var body: some View {
        AVPlayerControllerRepresented(player: player)
            .onAppear {
                player.play()
            }
            .onDisappear {
                player.pause()
                
            }
    }
}

struct VideoView_Previews: PreviewProvider {
    static var previews: some View {
        VideoView(url: MR.assetsVideos().subscribe.url)
    }
}

