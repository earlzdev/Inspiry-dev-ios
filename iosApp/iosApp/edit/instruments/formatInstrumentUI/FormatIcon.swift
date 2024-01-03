//
//  FormatIcon.swift
//  iosApp
//
//  Created by rst10h on 6.02.22.
//

import SwiftUI
import shared

struct FormatIcon: View {
    let colors: FormatInstrumentColors
    let dimens: FormatInstrumentDimens
    let format: DisplayTemplateFormat
    let color: SwiftUI.Color
    let hasPremium: Bool
    
    var body: some View {
        ZStack{
            RoundedRectangle(cornerRadius: 3)
                .stroke(lineWidth: 2.cg)
                .foregroundColor(color)
                .frame(width: format.iconWidthDp.cg, height: format.iconHeightDp.cg)
            
            if (format.premium && !hasPremium) {
                Image("ic_premium_template")
                    .resizable()
                    .frame(width: dimens.proWidth.cg, height: dimens.proHeight.cg)
                    .offset(x: dimens.proHorizontalOffset.cg, y: format.iconHeightDp.cg / (-2))
            }
        }
        .frame(height: dimens.iconHeight.cg)
        .frame(maxWidth: .infinity)
    }
}

struct FormatIcon_Previews: PreviewProvider {
    static var previews: some View {
        FormatIcon(
            colors: FormatInstrumentColorsLight(),
            dimens: FormatInstrumentDimensPhone(),
            format: FormatsProviderImpl().getFormats()[0],
            color: .white,
            hasPremium: false)
            .preferredColorScheme(.dark)
    }
}
