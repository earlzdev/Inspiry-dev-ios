//
//  UIScreen.swift
//  iosApp
//
//  Created by rst10h on 18.01.22.
//

import SwiftUI

extension UIScreen {
    static let screenWidth = UIScreen.main.bounds.size.width
    static let screenHeight = UIScreen.main.bounds.size.height
    static let screenSize = UIScreen.main.bounds.size
    
    static func getSafeArea(side: Edge.Set)->CGFloat{
        
        guard let root = UIApplication.shared.keyWindow?.rootViewController else {
            return 0
        }
        let topSafeArea: CGFloat
        let bottomSafeArea: CGFloat
        
        if #available(iOS 11.0, *) {
            topSafeArea = root.view.safeAreaInsets.top
            bottomSafeArea = root.view.safeAreaInsets.bottom
        } else {
            topSafeArea = root.topLayoutGuide.length
            bottomSafeArea = root.bottomLayoutGuide.length
        }
        
        if (side == .top) { return topSafeArea }
        if (side == .bottom) {return bottomSafeArea}
        
        return 0.cg
        
    }
}
