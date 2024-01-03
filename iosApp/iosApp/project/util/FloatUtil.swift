//
//  FloatUtil.swift
//  MusicFeatureIos
//
//  Created by vlad on 15/4/21.
//

import SwiftUI
import shared

extension CGFloat {
    func map(from: ClosedRange<CGFloat>, to: ClosedRange<CGFloat>) -> CGFloat {
        let result = ((self - from.lowerBound) / (from.upperBound - from.lowerBound)) * (to.upperBound - to.lowerBound) + to.lowerBound
        return result
    }
}

extension Int {
    var cg: CGFloat { return CGFloat(self) }
    var int32: Int32 { return Int32(self) }
    var int64: Int64 { return Int64(self) }
    var double: Double { return Double(self)}
    var ARGB: SwiftUI.Color {return Color.fromInt(self, withAlpha: true)}
}

extension Int32 {
    var cg: CGFloat { return CGFloat(self) }
    var int: Int { return Int(self) }
    var double: Double { return Double(self)}
    var float: Float { return Float(self)}
    var ARGB: SwiftUI.Color { return Color.fromInt(self.int, withAlpha: true)}
    var toKotlinInt: KotlinInt {return KotlinInt(value: self)}
}

extension Int64 {
    var cg: CGFloat { return CGFloat(self) }
    var int: Int { return Int(self) }
    var double: Double { return Double(self)}
    var float: Float { return Float(self)}
    var toKotlinLong: KotlinLong { return KotlinLong(value: self) }
}

extension Float {
    var cg: CGFloat { return CGFloat(self) }
    var double: Double { return Double(self) }
}

extension CGFloat {
    var float: Float { return Float(self) }

    var toInt: Int {
        return Int(self.rounded())
    }

    var toInt32: Int32 {
        return Int32(self.rounded())
    }
}

extension CGSize {
    var sharedSize: Size {
        return Size(width: self.width.toInt32, height: self.height.toInt32)
    }
}

extension DispatchTimeInterval {

    func toDouble() -> Double {
        switch(self) {
        case .seconds(let value):
            return Double(value)
        case .milliseconds(let value):
            return Double(value) / 1_000
        case .microseconds(let value):
            return Double(value) / 1_000_000
        case .nanoseconds(let value):
            return Double(value) / 1_000_000_000
        case .never:
            return -1
        @unknown default:
            return -1
        }
    }

}
