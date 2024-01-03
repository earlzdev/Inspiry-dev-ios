//
//  TopBarBackView.swift
//  iosApp
//
//  Created by vlad on 6/11/21.
//

import SwiftUI
import shared

struct TopBarBackView: View {
    
    let colors: EditColors
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

struct TopBarBackView_Previews: PreviewProvider {
    static var previews: some View {
        TopBarBackView(colors: EditColorsLight(), dimens: EditDimensPhone(), onNavigationBack: {})
    }
}
