//
//  PreviewProLabel.swift
//  iosApp
//
//  Created by vlad on 12/1/22.
//

import SwiftUI
import shared

struct PreviewProLabel: View {
    let colors: PreviewColors
    let dimens: PreviewDimens
    
    @State private var offset: CGFloat = 0
    
    private let textSize = CGFloat(100)
    
    var body: some View {
        
        let font = MR.fontsMont().bold.uiFont(withSize: Double(dimens.waterMarkInspiryText))
        let maxOffset = textSize + CGFloat(dimens.waterMarkInspiryItemBottomPadding)
        
        LazyVStack(alignment: .leading, spacing: 0) {
            
            ForEach((0..<10), content: { index in
                
                Text("INSPIRY")
                    .foregroundColor(colors.textWaterMark.toSColor())
                    .rotationEffect(Angle(degrees: -90))
                    .frame(width: textSize, height: textSize, alignment: Alignment.center)
                    .font(Font(font))
                    .padding(.bottom, CGFloat(dimens.waterMarkInspiryItemBottomPadding))
                    .opacity(Double(dimens.waterMarkInspiryTextOpacity))
                
            })
            
        }.offset(y: offset)
            .onAppear {
                withAnimation(Animation.linear(duration: 5).repeatForever(autoreverses: false)) { self.offset = -maxOffset }
            }
            .padding(.leading, CGFloat(dimens.waterMarkInspiryStartPadding))
            .frame(maxHeight: .infinity, alignment: .leading)
        
    }
}

struct PreviewProLabel_Previews: PreviewProvider {
    static var previews: some View {
        ZStack(alignment: .leading) {
            
            PreviewProLabel(colors: PreviewColorsLight(), dimens: PreviewDimensPhone())
        }
    }
}
