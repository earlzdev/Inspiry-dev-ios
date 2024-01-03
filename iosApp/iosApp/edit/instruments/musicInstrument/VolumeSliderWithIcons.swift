//
//  VolumeIcon.swift
//  iosApp
//
//  Created by rst10h on 28.04.22.
//

import SwiftUI

struct VolumeSliderWithIcons: View {
    @Binding
    var volume: Float
    let onLibraryClick: () -> Void
    
    var body: some View {
        CyborgImage(name: volume == 0 ? "ic_sound_off_wave_form_dialog_white" : "ic_sound_off_wave_from_dialog")
            .onTapGesture {
                if (volume == 0) {volume = 1}
                else {volume = 0}
            }
            .scaledToFill()
            .frame(width:  18, height: 14)
            .frame(width: 34.cg, height: 32.cg, alignment: .center)
            .background(0xff333333.ARGB)
            .cornerRadius(7)
        HStack {
            InspSlider(progress: $volume)
                .frame(height: 32.cg)
            CyborgImage(name: "ic_sound_on_wave_from_dialog")
                .scaledToFill()
                .frame(width:  18, height: 14)
            
            
        }
        .padding(.horizontal)
        .background(0xff333333.ARGB)
        .cornerRadius(7)
        
        CyborgImage(name: "ic_go_to_library_wave_from_dialog")
            .scaledToFill()
            .frame(width:  17, height: 18)
            .frame(width: 34.cg, height: 32.cg, alignment: .center)
            .background(0xff333333.ARGB)
            .cornerRadius(7)
            .onTapGesture {
                onLibraryClick()
            }
    }
}

//struct VolumeIcon_Previews: PreviewProvider {
//    static var samples: [Float] = (0...200).map { _ in Float(.random(in: 0...6)/10.0)}
//    static var previews: some View {
//        VolumeSliderWithIcons()
//            .environmentObject(MusicEditModelApple(musicPlayerModel: MusicPlayerViewModel(), templateView: nil))
//    }
//}
