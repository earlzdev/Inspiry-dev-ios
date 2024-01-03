//
//  VideoVolumePanel.swift
//  iosApp
//
//  Created by rst10h on 10.08.22.
//

import SwiftUI

struct VideoVolumePanel: View {
    @ObservedObject
    var model: VideoEditModelApple
    
    var body: some View {
        ZStack(alignment: Alignment(horizontal: .center, vertical: .top)) {
            InspVolumeSlider(volume: $model.volume)
                .padding(.horizontal, 30)
                .padding(.vertical, 30)
            PlayPauseButton(model: model)
                .offset(y: -60)
                .onTapGesture {
                    model.playPauseAction()
                }
        }
        .background(0xff292929.ARGB)
        //.frame(height: 100.cg)
            
    }
}
