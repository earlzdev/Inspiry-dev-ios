//
//  PreviewProgress.swift
//  iosApp
//
//  Created by vlad on 13/1/22.
//

import SwiftUI
import shared

struct PreviewProgress: View {
    
    let colors: PreviewColors
    let dimens: PreviewDimens
    
    let progress: CGFloat
    
    var body: some View {
        
        ZStack(alignment: .leading) {
            GeometryReader { geo in
                Color.white.frame(width: geo.size.width * progress)
                    .frame(maxHeight: .infinity)
                    .clipShape(RoundedRectangle(cornerRadius: CGFloat(dimens.progressHeight)))
            }
            
        }.frame(height: CGFloat(dimens.progressHeight))
            .frame(maxWidth: .infinity)
            .background(colors.progressBackground.toSColor().clipShape(RoundedRectangle(cornerRadius: CGFloat(dimens.progressHeight))))
    }
}

struct PreviewProgress_Previews: PreviewProvider {
    static var previews: some View {
        ZStack {
            PreviewProgress(colors: PreviewColorsLight(), dimens: PreviewDimensPhone(), progress: 0.5)
        }
        .frame(height: 300)
        .background(Color.black)
    }
}
