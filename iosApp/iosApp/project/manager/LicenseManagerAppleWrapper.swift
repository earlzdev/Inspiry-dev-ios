//
//  LicenseManagerAppleWrapper.swift
//  iosApp
//
//  Created by vlad on 12/11/21.
//

import Foundation
import shared

class LicenseManagerAppleWrapper: ObservableObject {
    
    @Published
    public private(set) var hasPremium: Bool
    
    let coreModel: LicenseManager
    
    init(coreModel: LicenseManager) {
        self.coreModel = coreModel
        self.hasPremium = coreModel.hasPremiumState.value as! Bool
        
        CoroutineUtil.watch(state: coreModel.hasPremiumState, onValueReceived: { [weak self] in
            self?.hasPremium = $0
        })
    }
}
