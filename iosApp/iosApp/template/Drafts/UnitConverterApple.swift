//
//  UnitConverterApple.swift
//  iosApp
//
//  Created by rst10h on 27.01.22.
//

import Foundation
import shared
import UIKit

class UnitConverterApple: BaseUnitsConverter {
    
    override func getMatchParentValue() -> Float {
        return SharedConstants.shared.MATCH_PARENT
    }
    
    override func getWrapContentValue() -> Float {
        return SharedConstants.shared.WRAP_CONTENT
    }
    
    override func getScreenWidth() -> Int32 {
        return UIScreen.screenWidth.toInt32
    }
    
    override func getScreenHeight() -> Int32 {
        return UIScreen.screenHeight.toInt32
    }
}
