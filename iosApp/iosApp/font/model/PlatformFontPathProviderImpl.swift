//
//  PlatformFontPathProviderImpl.swift
//  iosApp
//
//  Created by vlad on 14/7/21.
//

import Foundation
import shared

class PlatformFontPathProviderImpl : PlatformFontPathProvider {
    private let robotoFont = PredefinedFontPath(path: FontsManager.Companion().ROBOTO_FONT_PATH, displayName: FontsManager.Companion().ROBOTO_FONT_PATH.capitalized,
                                                regularId: FontResource(fontName: "roboto-regular.ttf", bundle: Bundle.main), italicId: FontResource(fontName: "roboto-italic.ttf", bundle: Bundle.main),
                                                lightId: FontResource(fontName: "roboto-light.ttf", bundle: Bundle.main), boldId: FontResource(fontName: "roboto-bold.ttf", bundle: Bundle.main), forPremium: false
    )
    private let sfProFont = PredefinedFontPath(path: FontsManager.Companion().SF_FONT_PATH,
                                               displayName: FontsManager.Companion().SF_FONT_NAME, regularId: nil, italicId: nil, lightId: nil, boldId: nil, forPremium: false)
    
    
    func getRobotoFont() -> PredefinedFontPath {
        return robotoFont
    }
    
    func getSfProFont() -> PredefinedFontPath {
        return sfProFont
    }
    
    func defaultFont() -> PredefinedFontPath {
        return sfProFont
    }
}
