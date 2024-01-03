//
//  GradientUtils.swift
//  iosApp
//
//  Created by rst10h on 3.02.22.
//

import SwiftUI
import shared

extension LinearGradient {
    static func fromPaletteLinearGradient(_ gradient: PaletteLinearGradient) -> Self {
        
        let coords = gradient.getShaderCoords(left: 0, top: 0, right: 1, bottom: 1)
        
        return LinearGradient(
            colors: gradient.colors.map {
                Color.fromInt($0.intValue)
            },
            startPoint: UnitPoint(
                x: CGFloat(coords.get(index: 0)),
                y: CGFloat(coords.get(index: 2))
            ),
            endPoint: UnitPoint(
                x: CGFloat(coords.get(index: 1)),
                y: CGFloat(coords.get(index: 3))
            )
        )
        
    }
}
