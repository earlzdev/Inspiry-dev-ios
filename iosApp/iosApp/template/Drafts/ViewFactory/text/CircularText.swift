//
//  CircularText.swift
//  iosApp
//
//  Created by rst10h on 2.11.22.
//

import Foundation

protocol CircularText { //todo move to kmm
    
    var circularRadius: Float { get set }
    var circularTextSize: Float  { get set }
    var circularCharPositions: [CircularCharPosition] { get set }
}

struct CircularCharPosition {
    var char: Character //character for drawing
    var charWidth: CGFloat //character width
    var charAngle: Float //character angle (0..360) (center?)
}
