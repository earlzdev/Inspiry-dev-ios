//
//  FontDialogButtonStyle.swift
//  iosApp
//
//  Created by vlad on 14/7/21.
//

import SwiftUI
import shared

struct FontDialogButtonStyle: View {
    let colors: FontDialogColors
    let dimens: FontDialogDimens
    let text: String
    let currentStyle: InspFontStyle
    let selectedStyle: InspFontStyle
    let onSelectedChange: (InspFontStyle) -> ()
    
    func getFontWeight() -> Font.Weight {
        switch currentStyle {
        case .bold:
            return Font.Weight.bold
        case .light:
            return Font.Weight.light
        default:
            return Font.Weight.regular
        }
    }
    
    var body: some View {
        
        let font: Font = Font.system(size: CGFloat(dimens.stylesFontSize), weight: getFontWeight())
        let modifiedFont = currentStyle == InspFontStyle.italic ? font.italic() : font
        let isSelected = currentStyle == selectedStyle
        
        FontDialogBaseStyleView(colors: colors, dimens: dimens, isSelected: isSelected, text: text, width: CGFloat(dimens.stylesBoxWidth), horizontalPadding: 0)
            .font(modifiedFont)
            .onTapGesture {
                onSelectedChange(currentStyle)
            }
    }
}

struct FontDialogButtonStyle_Previews: PreviewProvider {
    static var previews: some View {
        FontDialogButtonStyle(colors: FontDialogColorsDark(), dimens: FontDialogDimensPhone(),
                              text: "B", currentStyle: InspFontStyle.bold, selectedStyle: .bold, onSelectedChange: { _ in
                              })
    }
}
