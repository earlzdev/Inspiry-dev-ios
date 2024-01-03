//
//  VideoLooperView.swift
//  iosApp
//
//  Created by rst10h on 22.01.22.
//

import SwiftUI
import AVFoundation

struct LoopingPlayerView: UIViewRepresentable {
    var url: URL
    @Binding var isPlaying: Bool
    var videoGravity: AVLayerVideoGravity = .resizeAspect
    
    func updateUIView(_ looper: UIVideoPlayerLooper, context: Context) {
        if (isPlaying) {
            looper.play()
        } else {
            looper.stop()
        }
    }

    func makeUIView(context: Context) -> UIVideoPlayerLooper {
        let looper = UIVideoPlayerLooper(url: url, frame: .zero, videoGravity: videoGravity)
        if (isPlaying) { looper.play() }
        return looper
    }
}
