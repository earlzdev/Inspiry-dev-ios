//
//  RemoveBackgroundViewModel.swift
//  iosApp
//
//  Created by rst10h on 29.08.22.
//

import Foundation
import shared

class RemoveBgViewModelApple {

    public typealias RemoveBackgroundCallback = ([PickMediaResult]?, Error?) -> Void
    
    let coreModel: RemovingBgViewModel
    
    
    init(imagePaths: [String], source: String = "self", resultHandler: @escaping RemoveBackgroundCallback) {
        
        self.coreModel = RemovingBgViewModel(
            imagePaths: imagePaths,
            processor: RemoveBGProcessorImplApple(),
            externalResourceDao: Dependencies.resolveAuto(),
            analyticsManager: Dependencies.resolveAuto(),
            source: source,
            settings: Dependencies.resolveAuto(),
            fileSystem: Dependencies.resolveAuto()) { result in
                resultHandler(result, nil)
            }
    }
   
    func received(result: [PickMediaResult]) {
                    print("received image! \(result)")
        
    }
    
}
