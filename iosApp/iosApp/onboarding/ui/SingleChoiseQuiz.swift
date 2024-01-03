//
//  SingleChoiseQuiz.swift
//  iosApp
//
//  Created by rst10h on 18.01.22.
//

import SwiftUI
import shared

struct SingleChoiseQuiz: View {
    let data: OnBoardingDataQuiz
    @EnvironmentObject
    var model: OnboardingViewModelApple
    
    var body: some View {
        let btWidth = UIScreen.screenWidth * model.dimens.buttonContinueWidthPercent.cg - model.dimens.buttonContinuePaddingHorizontal.cg
        VStack (spacing: 0){
            OnBoardingTitle(data.title.localized(),
                            startColor: model.colors.textGradientStartColor.toSColor(),
                            endColor: model.colors.textGradientEndColor.toSColor(),
                            fontWeight: .bold)
                .padding(.top, model.dimens.animatedPaddingTop.cg)
                .padding(.bottom, model.dimens.animatedPaddingBottom.cg + 30)
            ForEach(data.choices.indices, id: \.self) { index in
                let choice = data.choices[index].localized()
                Button(
                    action: {
                        model.onQuizSelected(index)
                    }
                ) {
                    Text(choice)
                        .font(.system(size: model.dimens.firstQuizOption.cg))
                        .fontWeight(.bold)
                        .foregroundColor(Color.white)
                        .frame(width: btWidth, height: model.dimens.firstQuizOptionHeight.cg + model.dimens.optionSelectedBorder.cg * 2, alignment: .center)
                        .background(model.colors.firstQuizOptionBg.toSColor())
                        .cornerRadius(model.dimens.optionCorners.cg)
                        .padding(.vertical, model.dimens.firstQuizOptionPaddingVertical.cg)
                    
                }
            }           
        }
    }
}

struct SingleChoiseQuiz_Previews: PreviewProvider {
    static let data = OnBoardingDataQuiz(
        title: MR.strings().onboarding_quiz_1_title,
        choices: [
            MR.strings().onboarding_quiz_1_option_1,
            MR.strings().onboarding_quiz_1_option_2,
            MR.strings().onboarding_quiz_1_option_3
        ],
        singleChoice: true)
    static var previews: some View {
        SingleChoiseQuiz(data: data)
            .environmentObject(OnboardingViewModelApple())
    }
}
