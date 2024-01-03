//
//  InspSlider.swift
//  iosApp
//
//  Created by rst10h on 4.02.22.
//

import SwiftUI

struct InspSlider: View {
    @Binding
    var progress: Float
    @State
    var thumbPressed = false
       
    var body: some View {
        
        ValueSlider(value: $progress, onEditingChanged: { editing in thumbPressed = editing })
            .valueSliderStyle(
                HorizontalValueSliderStyle(
                    track: HorizontalValueTrack(
                        view: Color.white, mask: Capsule())
                        .background(0xff828282.ARGB)
                        .frame(height: 3)
                        .cornerRadius(1.5)
                    ,
                    thumb: Circle()
                        .fill(Color.fromInt(0x99828282))
                        .overlay(
                            Circle()
                                .fill(.white)
                                .frame(width: 10, height: 10)
                        )
                        .scaleEffect(thumbPressed ? 1.3 : 1)
                    ,
                    thumbSize: CGSize(width: 20, height: 20),
                    options: .interactiveTrack
                )
            )
    }
}

struct SimpleInspSlider: View {
    @State
    var progress: Float
    let onChange: (Float) -> ()
    
    var body: some View {
        let binding = Binding<Float>(
            get: { progress },
            set: { progress = $0
                onChange($0)
            }
        )
        InspSlider(progress: binding)
    }
    
}

struct InspSlider_Previews: PreviewProvider {
    
    static var previews: some View {
        InspSlider(progress: Binding<Float>(get: { 0.5 }, set: { _ in }))
        .preferredColorScheme(.dark)
        
        
    }
}
