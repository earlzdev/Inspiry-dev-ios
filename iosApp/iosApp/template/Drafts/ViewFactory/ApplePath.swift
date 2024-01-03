//
//  ApplePath.swift
//  iosApp
//
//  Created by rst10h on 28.01.22.
//

import Foundation
import shared
import SwiftUI

class ApplePath: CommonPath {
    
    var path = Path()
    var strokeWidth: Float = 1
    var color: SwiftUI.Color = .black
    var paintStyle: PaintStyle = .stroke
    var inverted: Bool = false
    private var cornerRadius: CGFloat = 0
    
    override func setStrokeWidth(strokeWidth: Float) {
        self.strokeWidth = strokeWidth
    }
    private var lastPoint: CGPoint? = nil
    private var firstPoint: CGPoint? = nil
    
    override func moveTo(x: Float, y: Float) {
        let x = round(x * 10) / 10
        let y = round(y * 10) / 10
        //if (path.isEmpty) { print("====start new path ==== \(lastPoint) \(firstPoint)") }
        //print("path move to \(x), \(y)")
        path.move(to: CGPoint(x: x.cg, y: y.cg))
        lastPoint = CGPoint(x: x.cg, y: y.cg)
    }
    
    override func close() {
        if (path.isEmpty) { return }
        
        if (cornerRadius != 0) {
            path.addQuadCurve(to: firstPoint!, control: lastPoint!)
        }
        
        path.closeSubpath() // todo?
        
        lastPoint = nil
        firstPoint = nil
        
        //print("====close path ====")
    }
    
    override func reset() {
        //print("====start new path reset====")
        path = Path()
        lastPoint = nil
        firstPoint = nil
    }
    override func updateFillType(inverse: Bool) {
        inverted = inverse
    }
    
    override func lineTo(x: Float, y: Float) {
        //if (path.isEmpty) { print("====start new path lineto====") }

        /**
         rounding x and y to tenths
         because it support currently only scuare rounded path, when (x and oldx) or (y and oldy) are eqal
         if x == 10.3 and oldx == 10.30002 it will be wrong and will be drawing bug
         */
        
        let x = round(x * 10) / 10
        let y = round(y * 10) / 10
        
        //print("path line to \(x), \(y)")
        
        if (cornerRadius == 0) {
            path.addLine(to: CGPoint(x: x.cg, y: y.cg))
        } else {
            var dx = 0.cg
            var dy = 0.cg
            if let lastPoint = self.lastPoint {
                let oldX = lastPoint.x
                let oldY = lastPoint.y
                dx = oldX - x.cg
                dy = oldY - y.cg
                
                if (dx == 0) {
                    if (dy != 0) {
                        dy = dy/abs(dy) * cornerRadius
                    } else {
                        dy = cornerRadius
                    }
                }
                
                if (dy == 0) {
                    if (dx != 0) {
                        dx = dx/abs(dx) * cornerRadius
                        
                    } else {
                        dx = cornerRadius
                    }
                }
                
                if firstPoint == nil {
                    firstPoint = CGPoint(x: oldX - dx, y: oldY - dy)
                    path.move(to: CGPoint(x: oldX - dx, y: oldY - dy))
                } else {
                    path.addQuadCurve(to: CGPoint(x: oldX - dx, y: oldY - dy), control: CGPoint(x: oldX, y: oldY))
                }
            }
            path.addLine(to: CGPoint(x: x.cg + dx, y: y.cg + dy))
            lastPoint = CGPoint(x: x.cg, y: y.cg)
        }
    }
    
    override func addOval(left: Float, right: Float, top: Float, bottom: Float) {
        path.addEllipse(in: CGRect(x: left.cg, y: top.cg, width: (right - left).cg, height: (bottom - top).cg))
    }
    
    override func quadTo(x: Float, y: Float, x2: Float, y2: Float) {
        path.addQuadCurve(to: CGPoint(x: x2.cg, y: y2.cg), control: CGPoint(x: x.cg, y: y.cg))
    }
    
    override func refreshGradient(gradient: PaletteLinearGradient?, width: Int32, height: Int32) {
        
    }
    
    override func setPathCornerRadius(absoluteRadius: Float) {
        self.cornerRadius = absoluteRadius.cg
    }
    
    override func refreshPathColor(color: Int32) {
        
    }
    
    override func addRoundRect(left: Float, top: Float, right: Float, bottom: Float, rx: Float, ry: Float) {
        path.addRoundedRect(
            in: CGRect(x: left.cg, y: top.cg, width: (right - left).cg, height: (bottom - top).cg),
            cornerSize: CGSize(width: rx.cg, height: ry.cg)
        )
    }
    
    override func addCircle(centerX: Float, centerY: Float, radius: Float) {
        let ellipseRect = CGRect(x: (centerX - radius).cg, y: (centerY - radius).cg, width: radius.cg * 2, height: radius.cg * 2)
        path.addEllipse(in: ellipseRect, transform: .identity)
    }
    
    override func refreshStyle(color: KotlinInt?, alpha: Float, strokeCap: String?, paintStyle: PaintStyle) {
        if let c = color {
            self.color = c.int32Value.ARGB
            //print ("set path color \(self.color.toHex())")
        }
        self.paintStyle = paintStyle
    }
    
    override func movePath(unitsConverter: BaseUnitsConverter, actualFrame: Float, movement: [PathMovement], pathLinePercents: NSMutableArray, movementsConnected: Bool, commonInterpolator: InspInterpolator?, v: InspView<AnyObject>) {
        super.movePath(unitsConverter: unitsConverter, actualFrame: actualFrame, movement: movement, pathLinePercents: pathLinePercents, movementsConnected: movementsConnected, commonInterpolator: commonInterpolator, v: v)
        
    }
}
