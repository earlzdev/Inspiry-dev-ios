//
//  EdItTextTopBar.swift
//  iosApp
//
//  Created by rst10h on 26.07.22.
//

import SwiftUI
import shared

struct EditTextTopBar: View {
    
    let colors: EditColors
    let dimens: EditDimens
    
    let onBack: () -> ()
    let onDone: () -> ()
    
    
    var body: some View {
        
        HStack {
            
            TopBarBackView(colors: colors, dimens: dimens, onNavigationBack: onBack)
            
            Spacer().frame(maxWidth: .infinity, maxHeight: .infinity)
            
            Button(action: {
                onDone()
                
            }) {
                Text(MR.strings().done.localized())
                    .font(.system(size: CGFloat(dimens.topBarTextSize), weight: .bold))
                    .foregroundColor(colors.keyboardDoneColor.toSColor())
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

struct EdItTextTopBar_Previews: PreviewProvider {
    static let colors: EditColors = EditColorsLight()
    static let dimens: EditDimens = EditDimensPhone()
    static var previews: some View {
        EditTextTopBar(colors: colors, dimens: dimens, onBack: {}, onDone: {})
    }
}
