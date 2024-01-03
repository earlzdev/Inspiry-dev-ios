//
//  InfoViewModelApple.swift
//  iosApp
//
//  Created by vlad on 5/11/21.
//

import Foundation
import shared

class InfoViewModelApple: ObservableObject {
    let coreModel: InfoViewModel?
    
    @Published
    var state: InspResponse<KotlinUnit>?
    
    init(coreModel: InfoViewModel?) {
        self.coreModel = coreModel
        
        self.state = (coreModel?.state.value as? InspResponse<KotlinUnit>)
        
        if let coreModel = coreModel {
            CoroutineUtil.watch(state: coreModel.state, onValueReceived: { [weak self] in self?.state = $0 })
        }
    }
}
