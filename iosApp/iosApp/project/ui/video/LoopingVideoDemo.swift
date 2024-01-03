//
//  LoopingVideoDemo.swift
//  iosApp
//
//  Created by rst10h on 22.01.22.
//

import SwiftUI
import shared

struct LoopingVideoDemo: View {
    @State var playing: Bool = true
    var body: some View {
        VStack {
            Button (action: {playing.toggle()}) {
                Text(playing ? "STOP" : "PLAY")
                    .padding(20)
                    .background(Color.gray.opacity(0.2))
            }
        LoopingPlayerView(url: MR.assetsVideosOnboarding().page_1.url, isPlaying: $playing)
                .aspectRatio(contentMode: .fit)
                .frame(height: 400)
        }
    }
}

struct LoopingVideoDemo_Previews: PreviewProvider {
    static var previews: some View {
        LoopingVideoDemo()
    }
}
