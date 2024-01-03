//
//  PlatformFontObtainerImpl.swift
//  iosApp
//
//  Created by vlad on 15/7/21.
//

import Foundation
import SwiftUI
import shared

class PlatformFontObtainerImpl: PlatformFontObtainer<UIFont> {
    
    override init(fontsManager: FontsManager, platformFontPathProvider: PlatformFontPathProvider) {
        super.init(fontsManager: fontsManager, platformFontPathProvider: platformFontPathProvider)
    }
    
    override func createDefaultTypeface(fontStyle: InspFontStyle?) -> UIFont {
        let ofSize: CGFloat = 0
        
        if fontStyle == nil || fontStyle == InspFontStyle.regular {
            return UIFont.systemFont(ofSize: ofSize, weight: UIFont.Weight.regular)
        }
        switch fontStyle! {
        case .bold:
            return UIFont.systemFont(ofSize: ofSize, weight: UIFont.Weight.bold)
        case .italic:
            return UIFont.italicSystemFont(ofSize: ofSize)
        case .light:
            return UIFont.systemFont(ofSize: ofSize, weight: UIFont.Weight.light)
            
        default:
            return UIFont.systemFont(ofSize: ofSize, weight: UIFont.Weight.regular)
        }
    }
    
    override func createFromFile(path: UploadedFontPath) throws -> UIFont {
        return createDefaultTypeface(fontStyle: .regular)
    }
    
    override func createResourceTypeface(fontResource: FontResource) -> UIFont {
        return fontResource.uiFont(withSize: 0)
    }

}
