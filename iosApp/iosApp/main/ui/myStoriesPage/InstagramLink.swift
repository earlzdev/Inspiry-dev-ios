//
//  InstagramLink.swift
//  iosApp
//
//  Created by rst10h on 30.12.21.
//

import SwiftUI
import shared

struct InstagramLink: View {
    @EnvironmentObject var viewModel: MainScreenViewModelApple
    var body: some View {
        let colors = viewModel.coreModel.getMainScreenColors()
        let dimens = viewModel.coreModel.getMainScreenDimens()
        VStack {
            Text(MR.strings().my_stories_subscribe_to_inst.localized())
                .foregroundColor(colors.instagramLinkTextColor.toSColor())
                .font(.system(size: CGFloat(dimens.instagramLinkText)))
                .multilineTextAlignment(TextAlignment.center)
            Button(action: {
                ExternalLinks.openInstagram()
            }, label: {
                Text(InstagramSubscribeHolderKt.INSTAGRAM_DISPLAY_NAME)
                    .foregroundColor(colors.instagramButtonText.toSColor())
                    .font(.system(size: CGFloat(dimens.instagramLinkText)))
                    .fontWeight(.bold)
                    .lineLimit(1)
                    .padding(.horizontal, 13)
                    .padding(.vertical, 2)
                    .background(colors.instagramButtonBack.toSColor())
                    .clipShape(RoundedRectangle(cornerRadius: 8))
            }
            )
        }
    }
    
}

struct InstagramLink_Previews: PreviewProvider {
    static var previews: some View {
        InstagramLink()
            .environmentObject(MainScreenViewModelApple.Create())
    }
}
