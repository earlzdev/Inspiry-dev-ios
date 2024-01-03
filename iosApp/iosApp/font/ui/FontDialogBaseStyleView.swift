//
//  FontDialogBaseStyleView.swift
//  iosApp
//
//  Created by vlad on 14/7/21.
//

import SwiftUI
import shared

struct FontDialogBaseStyleView: View {
    let colors: FontDialogColors
    let dimens: FontDialogDimens
    let isSelected: Bool
    let text: String
    let width: CGFloat?
    let horizontalPadding: CGFloat
    
    var body: some View {
        let borderColor = isSelected ? colors.styleBorderActive.toSColor() : colors.styleBorderInactive.toSColor()
        
        Text(text)
            .padding(.horizontal, horizontalPadding)
            .frame(width: width, height: CGFloat(dimens.stylesBoxHeight), alignment: .center)
            
            .foregroundColor(isSelected ? colors.styleTextActive.toSColor() : colors.styleTextInactive.toSColor())
            .lineLimit(1)
            .background(colors.styleBg.toSColor())
            .cornerRadius(CGFloat(dimens.stylesBoxCornerRadius))
            .overlay(
                RoundedRectangle(cornerRadius: CGFloat(dimens.stylesBoxCornerRadius))
                    .stroke(borderColor, lineWidth: CGFloat(dimens.stylesBoxBorderThickness))
            )
    }
}

struct FontDialogBaseStyleView_Previews: PreviewProvider {
    static var previews: some View {
        FontDialogBaseStyleView(colors: FontDialogColorsDark(), dimens: FontDialogDimensPhone(), isSelected: true, text: "AA", width: nil, horizontalPadding: 5)
    }
}
