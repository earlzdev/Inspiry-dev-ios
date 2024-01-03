//
//  StrokeModifier.swift
//  iosApp
//
//  Created by vlad on 18/1/22.
//

import Foundation
import SwiftUI


struct StrokeModifier<STYLE, SHAPE>: ViewModifier where STYLE: ShapeStyle, SHAPE: InsettableShape {
    
    let shape: SHAPE
    let shapeStyle: STYLE
    let lineWidth: CGFloat
   
    func body(content: Content) -> some View {
        content.overlay(shape.strokeBorder(shapeStyle, lineWidth: lineWidth))
    }
}
