//
//  InnerPathViewApple.swift
//  iosApp
//
//  Created by rst10h on 28.01.22.
//

import Foundation
import shared

class InnerPathViewApple: InnerViewPath {
    
    var media: MediaPath
    
    init(media: MediaPath) {
        self.media = media
    }
    
    func invalidateColorOrGradient() {
        
    }
      
    var drawPath: () -> CommonPath? = {
        return nil
    }

}
