//
//  MusicDownloadingViewModelApple.swift
//  iosApp
//
//  Created by vlad on 3/11/21.
//

import Foundation
import shared

class MusicDownloadingViewModelApple: ObservableObject {
    let coreModel: MusicDownloadingViewModel
    
    @Published
    public private(set) var downloadingState: InspResponse<TemplateMusic>
    
    init(_ coreModel: MusicDownloadingViewModel) {
        self.coreModel = coreModel
        self.downloadingState = coreModel.downloadingState.value as! InspResponse<TemplateMusic>
        
        CoroutineUtil.watch(state: coreModel.downloadingState, onValueReceived: { [weak self] in self?.downloadingState = $0 })
    }
}
