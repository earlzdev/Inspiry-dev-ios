//
//  ViewExtensions.swift
//  iosApp
//
//  Created by rst10h on 12.01.22.
//

import SwiftUI
import UIKit
import shared

extension View {
    // visibility modifier like in android
    @ViewBuilder func visibility(_ visibility: ViewVisibility) -> some View {
        if (visibility == .Visible) { self }
        if (visibility == .Invisible) { self.hidden() }
        
        //nothing if Gone
    }
    
    @ViewBuilder func isVisible(_ visibility: Bool) -> some View {
        if (visibility) { self } else
        { self.hidden() }
    }
    
    //Text linear gradient
    public func linearGradient(colors: [SwiftUI.Color], startPoint: UnitPoint, endPoint: UnitPoint) -> some View {
        self.overlay(
            LinearGradient(
                colors: colors,
                startPoint: startPoint,
                endPoint: endPoint)
        )
        .mask(self)
    }
    
    public func paletteGradient(_ gradient: PaletteLinearGradient) -> some View {
        self.overlay(gradient.getLinearGradient())
            .mask(self)
    }
}

enum ViewVisibility {
    case Visible, Invisible, Gone
}

extension UIScreen {
    
    static var statusBarHeight: CGFloat {
        let window = UIApplication.shared.windows.filter { $0.isKeyWindow }.first
        return window?.windowScene?.statusBarManager?.statusBarFrame.height ?? 0
    }
    
}

extension CGSize {
    func aspect() -> CGFloat {
        return width / height
    }
}
