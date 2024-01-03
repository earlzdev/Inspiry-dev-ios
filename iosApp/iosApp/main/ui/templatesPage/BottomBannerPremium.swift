//
//  BottomBannerPremium.swift
//  iosApp
//
//  Created by rst10h on 27.12.21.
//

import SwiftUI
import shared
import Toaster

struct BottomBannerPremium: View {
    @EnvironmentObject
    var viewModel: MainScreenViewModelApple
    
    var body: some View {
        let colors = viewModel.coreModel.getBannerColors()
        let font = MR.fontsNunito().bold.uiFont(withSize: 0)
        HStack {
            CyborgImage(name: "ic_bottom_banner_close")
                .scaledToFit()
                .frame(width: 40, height: 30, alignment: .trailing)
                .onTapGesture {
                    viewModel.coreModel.onRemoveBanner()
                }
            Spacer()
            VStack {
                Text(MR.strings().subscribe_try_days_button.localized())
                    .font(Font(font.withSize(15)))
                    .foregroundColor(colors.textColor.toSColor())
                Text(MR.strings().banner_trial_subtitle.localized())
                    .font(Font(font.withSize(12)))
                    .foregroundColor(colors.textColor.toSColor())
            }
            .padding(.trailing, 40)
            .padding(.vertical, 5)
            Spacer()
        }
        .background(
            LinearGradient(colors: [colors.gradientColor1.toSColor(), colors.gradientColor2.toSColor()], startPoint: .trailing, endPoint: .leading)
        )
        .clipShape(RoundedRectangle(cornerRadius:10))
        .padding(.horizontal, 30)
        
    }
}

struct BottomBannerPremium_Preview: PreviewProvider {
    static var previews: some View {
        BottomBannerPremium()
            .environmentObject(MainScreenViewModelApple.Create())
    }
}
