//
//  InspVolumeSlider.swift
//  iosApp
//
//  Created by rst10h on 10.08.22.
//

import SwiftUI

struct InspVolumeSlider: View {
    @Binding
    var volume: Float
    
    let height: CGFloat = 35
    
    var body: some View {
        HStack {
            CyborgImage(name: volume == 0 ? "ic_sound_off_wave_form_dialog_white" : "ic_sound_off_wave_from_dialog")
                .onTapGesture {
                    if (volume == 0) {volume = 1}
                    else {volume = 0}
                }
                .scaledToFit()
                .padding(height/4)
                .frame(height: height)
                .background(0xff333333.ARGB)
                .cornerRadius(height/5)
            HStack {
        InspSlider(progress: $volume)
                    .frame(height: height)
        CyborgImage(name: "ic_sound_on_wave_from_dialog")
            .scaledToFit()
            .frame(height: height/2.5)
            }
            .padding(.horizontal, height/3.5)
            .background(0xff333333.ARGB)
            .cornerRadius(height/5)
        }
        .frame(height: height)
    }
}

struct InspVolumeSlider_Previews: PreviewProvider {
    @State
    static var volume: Float = 1
    
    static var previews: some View {
        InspVolumeSlider(volume: $volume)
            .preferredColorScheme(.dark)
    }
}
