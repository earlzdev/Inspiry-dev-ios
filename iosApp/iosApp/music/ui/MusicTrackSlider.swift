//
//  MusicTrackSlider.swift
//  MusicFeatureIos
//
//  Created by vlad on 14/4/21.
//

import SwiftUI
import shared

struct MusicTrackSlider: View {
    let colors: MusicColors
    
    @Binding var value: CGFloat
    
    //(Bool) isDragging
    let onEditingChange: (Bool) -> ()
    
    @State private var lastOffset: CGFloat = 0
    private let trackOffset: CGFloat = 3
    
    private let knobSizeOutsideInactive: CGFloat = 12
    private let knobSizeInsideInactive: CGFloat = 6
    private let knobSizeOutsideActive: CGFloat = 18
    private let knobSizeInsideActive: CGFloat = 11
    
    private let range: ClosedRange<CGFloat> = 0...1
    
    @State private var isDragging: Bool = false
    
    var body: some View {
        
        GeometryReader { geometry in
            ZStack(alignment: .leading) {
                
                RoundedRectangle(cornerRadius: 10)
                    .frame(height: 2)
                    .frame(maxWidth: .infinity)
                    .foregroundColor(colors.musicTrackBg.toSColor())
                    .padding(.horizontal, trackOffset)
                
                let offsetClip = isDragging ? (self.knobSizeOutsideActive - self.knobSizeOutsideInactive) / 2: 0
                
                Circle()
                    .frame(width: isDragging ? knobSizeOutsideActive : knobSizeOutsideInactive, height: isDragging ? knobSizeOutsideActive : knobSizeOutsideInactive, alignment: .center)
                    .foregroundColor(colors.musicThumbColorOutside.toSColor())
                    .overlay(Circle()
                                .frame(width: isDragging ? knobSizeInsideActive : knobSizeInsideInactive, height: isDragging ? knobSizeInsideActive : knobSizeInsideInactive)
                                .foregroundColor(colors.musicThumbColorInside.toSColor())
                    )
                    
                    .offset(x: self.$value.wrappedValue.map(from: self.range, to: -offsetClip...(geometry.size.width - knobSizeOutsideInactive - offsetClip)))
                    
                    
                    .gesture(DragGesture(minimumDistance: 0).onChanged({ value in
                        
                        if (!self.isDragging) {
                            self.isDragging = true
                            onEditingChange(true)
                        }
                        
                        if abs(value.translation.width) < 0.1 {
                            self.lastOffset = self.$value.wrappedValue.map(from: self.range, to: 0...(geometry.size.width - self.knobSizeOutsideActive / 2))
                        }
                        
                        let sliderPos = max(0, min(self.lastOffset + value.translation.width, geometry.size.width - self.knobSizeOutsideActive / 2))
                        
                        let sliderVal = sliderPos.map(from: 0...(geometry.size.width - self.knobSizeOutsideInactive), to: self.range)
                        
                        self.value = sliderVal
                    }).onEnded({ it in
                        
                        self.isDragging = false
                        onEditingChange(false)
                        
                    }))
                
            }.frame(maxHeight: .infinity, alignment: .center)
        }
        
    }
}

struct MusicTrackSlider_Previews: PreviewProvider {
    @State
    static var progress: CGFloat = 0
    static var previews: some View {
        MusicTrackSlider(colors: MusicDarkColors(), value: $progress, onEditingChange: { _ in })
    }
}
