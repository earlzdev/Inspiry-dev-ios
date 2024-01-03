//
//  PlayPauseIcon.swift
//  iosApp
//
//  Created by rst10h on 28.04.22.
//

import SwiftUI

struct PlayPauseIcon: View {
    @Binding var isPlaying: Bool
    var body: some View {
        CyborgImage(name: isPlaying ? "ic_pause_wave_from_dialog" : "ic_play_wave_from_dialog")
            .scaledToFill()
            .frame(width:  11, height: 14)
            .frame(width: 30.cg, height: 42.cg, alignment: .center)
            .background(0xff333333.ARGB)
    }
}

struct PlayPauseIcon_Previews: PreviewProvider {
    @State
    static var isPlaying = false
    static var previews: some View {
        PlayPauseIcon(isPlaying: $isPlaying)
    }
}
