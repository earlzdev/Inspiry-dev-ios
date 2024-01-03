//
//  QuizView.swift
//  iosApp
//
//  Created by rst10h on 18.01.22.
//

import SwiftUI
import shared

struct QuizView: View {
    let data: OnBoardingDataQuiz
    let pageIndex: Int
    @EnvironmentObject
    var model: OnboardingViewModelApple
    let onSkip: () -> ()
    var body: some View {
        
        VStack(spacing: 0) {
            ZStack {
                DotsIndicator(pageIndex,
                              max: model.pagesCount,
                              activeSize: model.dimens.pageIndicatorSize.cg * 1.2,
                              inactiveSize: model.dimens.pageIndicatorSize.cg,
                              activeColor: model.colors.pageIndicatorActive.toSColor(),
                              inactiveColor: model.colors.pageIndicatorInactive.toSColor()
                )
                    .padding(.top, model.dimens.videoPromoIndicatorPaddingTop.cg)
                    .padding(.bottom, model.dimens.videoPromoIndicatorPaddingBottom.cg)
                
                
                HStack {
                    Spacer()
                    Button(action: { onSkip() }) {
                        Text(MR.strings().onboarding_quiz_skip.localized())
                            .font(.system(size: 14))
                            .underline()
                            .foregroundColor(model.colors.quizTextSkip.toSColor())
                            .padding(.trailing, 26)
                            .padding(.bottom, 3)
                    }
                }
                
            }
            Spacer()
            SingleChoiseQuiz(data: data)
            Spacer()
            Spacer()
            Text( MR.strings().onboarding_quiz_1_hint.localized())
                .foregroundColor(model.colors.firstQuizUsefulAnswers.toSColor())
                .font(.system(size: model.dimens.firstQuizUsefulAnswers_.cg))
                .multilineTextAlignment(.center)
                .frame(width: UIScreen.screenWidth * model.dimens.buttonContinueWidthPercent.cg)
                .padding(.bottom, model.dimens.firstQuizUsefulAnswersPaddingBottom.cg)
        }
    }
    
}

struct QuizView_Previews: PreviewProvider {
    static let data = OnBoardingDataQuiz(
        title: MR.strings().onboarding_quiz_1_title,
        choices: [
            MR.strings().onboarding_quiz_1_option_1,
            MR.strings().onboarding_quiz_1_option_2,
            MR.strings().onboarding_quiz_1_option_3
        ],
        singleChoice: true)
    static var previews: some View {
        QuizView(data: data, pageIndex: 2) {}
        .environmentObject(OnboardingViewModelApple())
    }
}
