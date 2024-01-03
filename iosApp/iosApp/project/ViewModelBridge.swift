//
//  AppleViewModel.swift
//  iosApp
//
//  Created by vlad on 16/7/21.
//

import Foundation
import shared

open class ViewModelBridge<CoreModel: Mvvm_coreViewModel>: ObservableObject {
    
    let coreModel: CoreModel
    init (_ coreModel: CoreModel) {
        self.coreModel = coreModel
    }
    
    deinit {
        coreModel.onCleared()
    }
}
