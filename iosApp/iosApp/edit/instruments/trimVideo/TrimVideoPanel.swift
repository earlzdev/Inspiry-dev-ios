//
//  TrimVideoPanel.swift
//  iosApp
//
//  Created by rst10h on 28.07.22.
//

import SwiftUI
import shared

struct TrimVideoPanel: View {
    
    var model: VideoEditModelApple
    
    var body: some View {
        ZStack(alignment: Alignment(horizontal: .center, vertical: .top)) {
            VStack(spacing: 0) {
                HStack {
                        TimePresetsVideo(model: model)
                    Spacer()
                        TimeLabel(model: model)
                        .font(.system(size: 12.5))
                            .foregroundColor(0xffffffff.ARGB)
                            .padding(5.cg)
                            .background(0xff303030.ARGB)
                            .cornerRadius(4)
                        
                }
                .padding(.top, 5)
                .padding(.bottom, 10)
                .padding(.horizontal, 15)
                ScalableTrimSlider(model: model)
                    .onDisappear {
                        model.onDisappear()
                    }
                    .frame(height: 70)
            }
            PlayPauseButton(model: model)
                .offset(y: -90)
                .onTapGesture {
                    model.playPauseAction()
                }
        }
        .padding(.vertical, 10)
        .background(0xff292929.ARGB)
    }
}

struct TimeLabel: View {
    @ObservedObject
    var model: VideoEditModelApple
    
    var body: some View {
        let time = model.trimmedDurationMs * (model.progress ?? 1).double
        let timems = time < 2000 ? ".\(String(format: "%02d", Int((time / 10).truncatingRemainder(dividingBy: 100))))" : ""
        Text(TrackUtils.init().convertTimeToString(durationMs: Int64(time)) + timems)
    }
    
    
}

struct TimePresetsVideo: View {

    @ObservedObject
    var model: VideoEditModelApple
    
    var body: some View {
        HStack {
            ForEach(VideoEditModelApple.predefinedDurationsSec, id: \.self) { sec in
                let text = sec > 0 ? String(format: "%.1fs", sec) : getPredefinedText(value: sec)
                Text(text)
                    .font(.system(size: 12.5))
                    .foregroundColor(Color.white)
                    .padding(5.cg)
                    .background(0xff3d3d3d.ARGB)
                    .cornerRadius(5)
                    .onTapGesture {
                        model.setNewRange(durationSec: sec)
                    }
                    .padding(.trailing, 10)
            }
        }
    }
    
    private func getPredefinedText(value: Double) -> String {
        let type = CalculatedDurations(rawValue: value)
        switch(type) {
        case .Template:
            return MR.strings().trim_template.localized()
        case .VideoDuration:
            return MR.strings().trim_fullVideo.localized()
        case .none:
            return "--"
        }
    }
}

struct PlayPauseButton: View {
    
    @ObservedObject
    var model: VideoEditModelApple
    
    var body: some View {
        CyborgImage(name: model.isPlaying ? "ic_stop_trim_page" : "ic_play_trim_page")
            .padding(10)
            .background(0xff333333.ARGB.opacity(0.7))
            .cornerRadius(12)
            .frame(width: 40, height: 40, alignment: .center)
        
    }
}
