//
//  LicenseManagerApple.swift
//  iosApp
//
//  Created by vlad on 12/11/21.
//

import Foundation
import shared
import Adapty

class LicenseManagerApple: LicenseManagerImpl, ObservableObject {
    
    override func restorePurchases(forceUpdate: Bool) {

        Adapty.restorePurchases() { result in
            switch result {
            case .failure(let error):
                print("Adapty error restoring purchases \(error.localizedDescription)")
            case .success(let info):
                    let accessLevel = info.accessLevels[LicenseManagerImplKt.DEFAULT_ADAPTY_ACCESS]
                    
                    if (accessLevel?.isActive != nil) {
                        let hasPremium = accessLevel!.isActive
                        
                        self.onGotPurchasesResultRemote(hasPremium: hasPremium, id: accessLevel!.vendorProductId)
                    }
                }
        }
    }
}
