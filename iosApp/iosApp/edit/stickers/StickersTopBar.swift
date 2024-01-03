//
//  StickersTopBar.swift
//  iosApp
//
//  Created by rst10h on 6.09.22.
//

import SwiftUI
import shared

struct StickersTopBar: View {
    
    let colors: StickersColors
    let dimens: EditDimens
    
    let onBack: () -> ()
    let onDone: () -> ()
    
    
    var body: some View {
        
        HStack {
            
            StickersBack(colors: colors, dimens: dimens, onNavigationBack: onBack)
            
            Spacer().frame(maxWidth: .infinity, maxHeight: .infinity)
            
            Button(action: {
                onDone()
                
            }) {
                Text(MR.strings().save.localized())
                    .font(.system(size: CGFloat(dimens.topBarTextSize), weight: .bold))
                    .foregroundColor(colors.topBarText.toSColor())
                    .lineLimit(1)
                    .padding(.horizontal, 16)
                    .fixedSize()
                
            }
            .padding(.trailing, 14)
            .frame(maxHeight: .infinity)
        }
        .frame(height: CGFloat(dimens.topBarHeight))
    }
}

struct StickersBack: View {
    
    let colors: StickersColors
    let dimens: EditDimens
    let onNavigationBack: () -> ()
    
    var body: some View {
        Button(action: onNavigationBack, label: {
            HStack(spacing: 0) {
                SVGImage(svgName: "ic_arrow_back_edit", contentMode: .center)
                    .frame(width: 30)
                Text(MR.strings().back.localized())
                    .foregroundColor(colors.topBarText.toSColor())
                    .font(.system(size: CGFloat(dimens.topBarTextSize), weight: .bold))
                    .lineLimit(1)
            }
            
        }) .frame(maxHeight: .infinity)
            .padding(.horizontal, 16)
    }
}

struct StickersTopBar_preview: PreviewProvider {
    static let colors: StickersColors = StickersDarkColors()
    static let dimens: EditDimens = EditDimensPhone()
    static var previews: some View {
        StickersTopBar(colors: colors, dimens: dimens, onBack: {}, onDone: {})
    }
}
