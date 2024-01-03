//
//  VideoDemoUI.swift
//  iosApp
//
//  Created by rst10h on 22.06.22.
//

import SwiftUI
import Kingfisher
import shared

struct VideoDemoUI: UIViewRepresentable {
    let inspView: InspSimpleVideoView
          
    init(inspView: InspSimpleVideoView) {
        self.inspView = inspView

    }
    
    func makeUIView(context: UIViewRepresentableContext<VideoDemoUI>) -> UIView {

        let player = inspView.player as! VideoPlayerApple
        return player.looper!
    }
    
    func updateUIView(_ uiView: UIView, context: UIViewRepresentableContext<VideoDemoUI>) {
        
    }
}
