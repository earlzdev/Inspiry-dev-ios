//
//  ExportView.swift
//  iosApp
//
//  Created by vlad on 6/11/21.
//

import SwiftUI
import shared

//todo remove it?
struct ExportView: View {
    let templateModel: InspTemplateViewApple
    let colors: EditColors
    let dimens: EditDimens = EditDimensPhone()
    let onNavigationBack: () -> ()

    var body: some View {

        VStack {

            HStack {

            TopBarBackView(colors: colors, dimens: dimens, onNavigationBack: onNavigationBack)

            }.frame(height: 60)
                    .frame(maxWidth: .infinity, alignment: .leading)

            EditExportTemplateView(templateModel: templateModel)
                    .padding(.bottom, 0.0)
                    .padding(.top, 0.0)

            
            //ExportBottomPanel(colors: colors, dimens: dimens)

        }.frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

extension View {
    func exportBottomPanelShape(colors: EditColors, dimens: EditDimens) -> some View {
        
        self
            .frame(maxWidth: .infinity)
            .padding(.bottom, getBottomScreenInset())
            .background(colors.exportBottomPanelBg.toSColor())
            .clipShape(RoundedCorner(radius: CGFloat(dimens.exportCornerRadius), corners: .topRight))
            .clipShape(RoundedCorner(radius: CGFloat(dimens.exportCornerRadius), corners: .topLeft))
    }
}

//struct ExportView_Previews: PreviewProvider {
//    static var previews: some View {
//        ExportView(template: getTestTemplate(), colors: EditColorsLight(), onNavigationBack: {})
//    }
//}
