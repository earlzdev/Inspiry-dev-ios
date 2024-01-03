//
//  SubscribeHeader.swift
//  iosApp
//
//  Created by rst10h on 5.12.22.
//

import Foundation
import SwiftUI
import shared

struct SubscribeHeader: View {
    let colors: SubscribeColors
    let dimens: SubscribeDimens
    let onNavigationBack: () -> ()
    @State
    var isPlaying: Bool = true
    var body: some View {
        ZStack(alignment: .top) {
            LoopingPlayerView(url: MR.assetsVideos().subscribe.url, isPlaying: $isPlaying)
                .aspectRatio(contentMode: .fill)
                .onAppear() {
                    isPlaying = true
                }
                .onDisappear {
                    isPlaying = false
                }
                .frame(maxWidth: .infinity)
            VStack(spacing: 20.cg) {
                Spacer()
                Text(MR.strings().banner_my_stories_title.localized()
                    .uppercased())
                .font(.system(size: dimens.headerTextTitle.cg))
                .fontWeight(.heavy)
                .foregroundColor(Color.white)
                .multilineTextAlignment(.center)
                .shadow(radius: 5.cg)
                .padding(.horizontal, dimens.headerTextStartEndPadding.cg * 1.5)
                Text(MR.strings().subscribe_header_subtitle.localized())
                .font(.system(size: dimens.headerTextSubTitle.cg))
                .bold()
                .foregroundColor(Color.white)
                .multilineTextAlignment(.center)
                .shadow(radius: 5.cg)
                .padding(.horizontal, dimens.headerTextStartEndPadding.cg * 1.5)
                .padding(.bottom, UIScreen.statusBarHeight)

            }
        }
        .cornerRadius(dimens.headerCorners.cg, corners: [.bottomLeft, .bottomRight])
    }
}
