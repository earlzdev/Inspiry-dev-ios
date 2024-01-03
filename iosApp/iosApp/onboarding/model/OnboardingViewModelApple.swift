//
//  OnboardingViewModelApple.swift
//  iosApp
//
//  Created by rst10h on 17.01.22.
//

import Foundation
import shared

class OnboardingViewModelApple: ObservableObject {
    
    @Published
    var currentPage: Int = 0
    
    var pagesCount: Int {
        return coreModel.pagesData.count
    }
    
    private let coreModel: OnBoardingViewModel
    
    public let colors = OnBoardingColorsLight()
    public let dimens = OnBoardingDimensPhone()
    
    init() {
        let settings: Settings = Dependencies.resolveAuto()
        let licenseManager: LicenseManager = Dependencies.resolveAuto()
        let analyticsManager: AnalyticsManager = Dependencies.resolveAuto()
        let loggerGetter: LoggerGetter = Dependencies.resolveAuto()
        
        coreModel = OnBoardingViewModel.companion.create(
            settings: settings,
            licenseManager: licenseManager,
            analyticsManager: analyticsManager,
            loggerGetter: loggerGetter,
            stringToEnLocale: { it in
                return it.localized(lang: "en") ?? it.localized()
            },
            onFinish: {
                print("finish")
            },
            onOpenSubscribe: { _ in })
        
        coreModel.step = {
            return KotlinInt(value: Int32(self.currentPage))
            
        }
        
        coreModel.nextStep = {
            guard self.currentPage < ( self.pagesCount - 1) else { return }
            self.currentPage+=1
            
        }
        coreModel.prevStep = {
            guard self.currentPage > 0 else { return }
            self.currentPage-=1
            
        }
    }
       
    func doOnOpenSubscribe(_ action: @escaping (String) -> ()) {
        coreModel.doOnSubscribe (action: action)
    }
    
    func getPage() -> String {
        let page: OnBoardingDataVideo = coreModel.pagesData[0] as! OnBoardingDataVideo
        return page.text.localized()
    }
    
    func getDataByIndex(_ index: Int) -> OnBoardingData {
        return coreModel.pagesData[index]
    }
    
    func nextPage() {
        coreModel.nextStep()
    }
    
    func onQuizSelected(_ index: Int) {
        coreModel.onFirstQuizSelected(index: Int32(index))
    }
}
