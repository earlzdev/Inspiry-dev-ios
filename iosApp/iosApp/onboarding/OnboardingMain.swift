//
//  OnboardingMain.swift
//  iosApp
//
//  Created by rst10h on 17.01.22.
//

import SwiftUI
import AVFoundation
import shared

struct OnboardingMain: View {
    
    @Binding var fullScreenSelection: String?
    
    @StateObject
    var model = OnboardingViewModelApple()
    
    var body: some View {
        TabView(selection: $model.currentPage) {
            ForEach(0..<model.pagesCount, id: \.self) { index in
                
                let data = model.getDataByIndex(index)
                Group {
                    if (data is OnBoardingDataVideo) {
                        VideoPromo(data: data as! OnBoardingDataVideo, pageIndex: index)
                    }
                    if (data is OnBoardingDataQuiz) {
                        QuizView(data: data as! OnBoardingDataQuiz, pageIndex: index) {
                            fullScreenSelection = Constants.TAG_SUBSCRIBE_VIEW
                        }
                    }
                }
                .frame(width: UIScreen.screenWidth, alignment: .center)
                .tag(index)
            }
        }
        .tabViewStyle(PageTabViewStyle(indexDisplayMode: .never))
        .progressViewStyle(.circular)
        .environmentObject(model)
        .onAppear {
            model.doOnOpenSubscribe { it in
                fullScreenSelection = Constants.TAG_SUBSCRIBE_VIEW
            }
        }
    }
}

struct OnboardingMain_Previews: PreviewProvider {
    @State static var fullScreenSelection: String? = Constants.TAG_ONBOARDING_VIEW
    static var previews: some View {
        OnboardingMain(fullScreenSelection: $fullScreenSelection)
    }
}
