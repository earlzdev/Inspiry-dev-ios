//
//  ExportTagUsView.swift
//  iosApp
//
//  Created by vlad on 10/12/21.
//

import SwiftUI
import shared

struct ExportTagUsView: View {
    let colors: EditColors
    let dimens: EditDimens
    let onClick: () -> ()
    
    var body: some View {
        Button(action: onClick) {
            VStack(spacing: 0) {
                
                HStack(spacing: CGFloat(dimens.exportTagUsBetweenPadding)) {
                    
                    Text(MR.strings().saving_tag_us_inspiry1.localized())
                        .foregroundColor(colors.exportToAppText.toSColor())
                        .font(.system(size: CGFloat(dimens.exportChoiceTextSize)))
                        .lineLimit(1)
                    
                    Text(InstagramSubscribeHolderKt.INSTAGRAM_DISPLAY_NAME)
                        .foregroundColor(colors.exportImageElseVideoSelectedText.toSColor())
                        .font(.system(size: CGFloat(dimens.exportTagUsInspiryText), weight: .medium))
                        .padding(.bottom, 2)
                        .padding(.horizontal, 8)
                        .background(LinearGradient(colors: [colors.exportProgressStart.toSColor(), colors.exportProgressEnd.toSColor()], startPoint: UnitPoint(x: 0, y: 0.5), endPoint: UnitPoint(x: 1, y: 0.5)))
                        .clipShape(RoundedRectangle(cornerRadius: CGFloat(dimens.exportTagUsInspiryBgCornerRadius)))
                    
                }
                
                Text(MR.strings().saving_tag_us_inspiry2.localized())
                    .foregroundColor(colors.exportToAppText.toSColor())
                    .font(.system(size: CGFloat(dimens.exportChoiceTextSize)))
                    .lineLimit(1)
                    .padding(.horizontal, 4)
                    .padding(.vertical, 3)
            }.padding(.horizontal, 10)
        }
    }
}

struct ExportTagUsView_Previews: PreviewProvider {
    static var previews: some View {
        let colors = EditColorsLight()
        let dimens = EditDimensPhone()
        
        ExportTagUsView(colors: colors, dimens: dimens, onClick: {}).exportBottomPanelShape(colors: colors, dimens: dimens)
    }
}
