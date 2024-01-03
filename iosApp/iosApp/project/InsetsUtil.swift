//
//  InsetsUtil.swift
//  iosApp
//
//  Created by vlad on 20/1/22.
//

import Foundation
import UIKit

func getBottomScreenInset() -> CGFloat {
    let window = getCurrentWindow()
    
    // reduce bottom inset a little bit
    return (window?.safeAreaInsets.bottom ?? 0) * 0.7
}

func getCurrentWindow() -> UIWindow? {
    return UIApplication.shared.connectedScenes
        .filter({$0.activationState == .foregroundActive || $0.activationState == .foregroundInactive})
            .compactMap({$0 as? UIWindowScene})
            .first?.windows
            .filter({$0.isKeyWindow}).first
}

