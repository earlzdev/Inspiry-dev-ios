//
//  SwiftMathExtensions.swift
//  iosApp
//
//  Created by rst10h on 13.07.22.
//

import Foundation
import SpriteKit
import SwiftUI

extension CGVector {
    func angleBetween(_ other: CGVector) -> Angle {
        let rad = atan2(self.dx * other.dy - self.dy * other.dx, self.dx * other.dx + self.dy * other.dy)
        return Angle(radians: rad)
    }
}

extension CGPoint {
    func rotation ( _ angle: Angle, arround: CGPoint = CGPoint(x: 0,y: 0)) -> CGPoint {
        let newx = arround.x + (self.x-arround.x) * cos( angle.radians ) - (self.y-arround.y ) * sin( angle.radians )
        let newy = arround.y + (self.x-arround.x) * sin( angle.radians ) + (self.y-arround.y ) * cos( angle.radians )
        return CGPoint(x: newx, y: newy)
    }
}
