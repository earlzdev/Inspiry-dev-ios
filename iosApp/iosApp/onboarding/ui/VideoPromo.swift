//
//  VideoPromo.swift
//  iosApp
//
//  Created by rst10h on 17.01.22.
//

import SwiftUI
import shared

struct VideoPromo: View {
    let data: OnBoardingDataVideo
    let pageIndex: Int
    @EnvironmentObject
    var model: OnboardingViewModelApple
    @State var playing = true
    
    var body: some View {
        let ratio: CGFloat = data.videoWidth.cg / data.videoHeight.cg
        VStack {
            Spacer()
            LoopingPlayerView(url: data.video.url, isPlaying: $playing)
                .aspectRatio(ratio, contentMode: .fit)
                .onAppear() {
                    playing = true
                }
                .onDisappear {
                    playing = false
                }
            Spacer()
            
            OnBoardingTitle(data.text.localized(),
                            startColor: model.colors.textGradientStartColor.toSColor(),
                            endColor: model.colors.textGradientEndColor.toSColor(),
                            fontWeight: .medium)
                .padding(.top, model.dimens.animatedPaddingTop.cg)
                .padding(.bottom, model.dimens.animatedPaddingBottom.cg)
            
            Button( action: {
                withAnimation {
                    model.nextPage()
                }
            }) {
                let btWidth = UIScreen.screenWidth * model.dimens.buttonContinueWidthPercent.cg - model.dimens.buttonContinuePaddingHorizontal.cg
                Text(MR.strings().subscribe_continue_button.localized().capitalized)
                    .foregroundColor(Color.white)
                    .font(.system(size: model.dimens.buttonContinueTextSize.cg))
                    .fontWeight(.heavy)
                    .frame(width: btWidth, height: model.dimens.buttonContinueHeight.cg, alignment: .center)
                    .background(LinearGradient (colors: [
                        model.colors.videoPromoContinueGradientStart.toSColor(),
                        model.colors.videoPromoContinueGradientEnd.toSColor()
                    ], startPoint: .leading, endPoint: .trailing))
                    .cornerRadius(model.dimens.buttonContinueCorners.cg)
                    .padding(.bottom)
            }
            
            DotsIndicator(pageIndex,
                          max: model.pagesCount,
                          activeSize: model.dimens.pageIndicatorSize.cg * 1.2,
                          inactiveSize: model.dimens.pageIndicatorSize.cg,
                          activeColor: model.colors.pageIndicatorActive.toSColor(),
                          inactiveColor: model.colors.pageIndicatorInactive.toSColor()
            )
                .padding(.bottom, UIScreen.getSafeArea(side: .bottom) != 0 ? 0 : model.dimens.videoPromoIndicatorPaddingBottom.cg)
        }
    }
}

struct VideoPromo_Previews: PreviewProvider {
    static let data = OnBoardingDataVideo(
        video: MR.assetsVideosOnboarding().page_1,
        videoHeight: 1374,
        text: MR.strings().onboarding_text_1)
    static let pageIndex = 1
    static var previews: some View {
        VideoPromo(data: data, pageIndex: pageIndex)
            .environmentObject(OnboardingViewModelApple())
    }
}
