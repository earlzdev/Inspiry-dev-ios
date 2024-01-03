//
//  FontStylesView.swift
//  iosApp
//
//  Created by vlad on 14/7/21.
//

import SwiftUI
import shared

struct FontStylesView: View {
    
    let colors: FontDialogColors
    let dimens: FontDialogDimens
    let font: FontPath
    let text: String
    let selectedStyle: InspFontStyle
    let platformFontPathProvider: PlatformFontPathProvider
    let textCaseHelper: TextCaseHelper
    let onSelectedChange: (InspFontStyle) -> ()
    let onToggleCapsMode: () -> ()
    
    
    var body: some View {
        HStack(spacing: CGFloat(dimens.stylesBoxPaddingHorizontal)) {
            if font.supportsBold(platformFontPathProvider: platformFontPathProvider) {
                FontDialogButtonStyle(colors: colors, dimens: dimens, text: "B", currentStyle: .bold, selectedStyle: selectedStyle, onSelectedChange: { it in
                    onSelectedChange(it)
                })
            }
            if font.supportsItalic(platformFontPathProvider: platformFontPathProvider) {
                FontDialogButtonStyle(colors: colors, dimens: dimens, text: "I", currentStyle: .italic, selectedStyle: selectedStyle, onSelectedChange: { it in
                    onSelectedChange(it)
                })
            }
            
            if font.supportsLight(platformFontPathProvider: platformFontPathProvider) {
                FontDialogButtonStyle(colors: colors, dimens: dimens, text: "L", currentStyle: .light, selectedStyle: selectedStyle, onSelectedChange: { it in
                    onSelectedChange(it)
                })
            }
            
            FontDialogBaseStyleView(colors: colors, dimens: dimens, isSelected: true, text: textCaseHelper.setCaseBasedOnOther(value: "aa", other: text), width: nil, horizontalPadding: CGFloat(dimens.stylesBoxTextCasePaddingHorizontal)).onTapGesture {
                onToggleCapsMode()
            }
            
        }.padding(.top, CGFloat(dimens.stylesBoxPaddingTop))
        .frame(height: CGFloat(dimens.stylesSectionHeight), alignment: Alignment(horizontal: .center, vertical: .top))
        
    }
}

struct FontStylesView_Previews: PreviewProvider {
    
    static var previews: some View {
        
        let fontPathProvider = Dependencies.diContainer.resolve(PlatformFontPathProvider.self)!
        let textCaseHelper = Dependencies.diContainer.resolve(TextCaseHelper.self)!
        
        let defaultFont = fontPathProvider.defaultFont()
        
        FontStylesView(colors: FontDialogColorsDark(),
                       dimens: FontDialogDimensPhone(),
                       font: defaultFont, text: "Some text", selectedStyle: InspFontStyle.bold, platformFontPathProvider: fontPathProvider, textCaseHelper: textCaseHelper, onSelectedChange: { _ in }, onToggleCapsMode: {})
    }
}
