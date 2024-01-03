//
//  ColorUtils.swift
//  MusicFeatureIos
//
//  Created by vlad on 7/4/21.
//

import shared
import SwiftUI
import UIKit
import Lottie
import Foundation

extension shared.Color {
    
    func toSColor() -> SwiftUI.Color {
        return SwiftUI.Color(red: (Double(red) / 255.0), green: (Double(green) / 255.0),
                             blue: (Double(blue) / 255.0), opacity: (Double(alpha) / 255.0))
    }
    
    func toUIColor() -> UIColor {
        let divider: CGFloat = 255
        return UIColor(red: (CGFloat(red) / divider), green: (CGFloat(green) / divider),
                       blue: (CGFloat(blue) / divider), alpha: (CGFloat(alpha) / divider))
    }
}

extension Int32 {
    func toLottieColor() -> LottieColor {
        let a = (self >> 24) & 0xff
        let r = (self >> 16) & 0xff
        let g = (self >> 8) & 0xff
        let b = self & 0xff
        
        return LottieColor(r: Double(r)/255.0, g: Double(g)/255.0, b: Double(b)/255.0, a: Double(a)/255.0)
    }
}

extension SwiftUI.Color {
    //ARGB int to color
    static func fromInt(_ intColor: Int, withAlpha: Bool = false) -> SwiftUI.Color {
        if intColor == 0 { return  Color.clear }
        let a = withAlpha ? (intColor >> 24) & 0xff : 0xff
        let r = (intColor >> 16) & 0xff
        let g = (intColor >> 8) & 0xff
        let b = intColor & 0xff
        
        return Color.init(red: Double(r)/255.0, green: Double(g)/255.0, blue: Double(b)/255.0, opacity: Double(a)/255.0)
    }
    
    func toRGBInt() -> Int? {
        let col = UIColor(cgColor: self.cgColor!)
        var fRed : CGFloat = 0
        var fGreen : CGFloat = 0
        var fBlue : CGFloat = 0
        var fAlpha: CGFloat = 0
        if col.getRed(&fRed, green: &fGreen, blue: &fBlue, alpha: &fAlpha) {
            let r = Int(fRed * 255.0)
            let g = Int(fGreen * 255.0)
            let b = Int(fBlue * 255.0)
            let a = Int(fAlpha * 255.0)
            let rgb = (a << 24) + (r << 16) + (g << 8) + b
            return rgb
        } else {
            return nil
        }
    }
    
    func toHex() -> String {
        let comp = self.cgColor!.components
        var str = ""
        comp!.forEach { f in
            str += String(format:"%02X", Int(f * 255))
        }
        return str
    }
}

public extension UIImage {
    convenience init?(color: UIColor, size: CGSize = CGSize(width: 1, height: 1)) {
        let rect = CGRect(origin: .zero, size: size)
        UIGraphicsBeginImageContextWithOptions(rect.size, false, 0.0)
        color.setFill()
        UIRectFill(rect)
        let image = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        
        guard let cgImage = image?.cgImage else { return nil }
        self.init(cgImage: cgImage)
    }
        
        func cropAlpha() -> UIImage {
            
            let cgImage = self.cgImage!;
            
            let width = cgImage.width
            let height = cgImage.height
            
            let colorSpace = CGColorSpaceCreateDeviceRGB()
            let bytesPerPixel:Int = 4
            let bytesPerRow = bytesPerPixel * width
            let bitsPerComponent = 8
            let bitmapInfo: UInt32 = CGImageAlphaInfo.premultipliedLast.rawValue | CGBitmapInfo.byteOrder32Big.rawValue
            
            guard let context = CGContext(data: nil, width: width, height: height, bitsPerComponent: bitsPerComponent, bytesPerRow: bytesPerRow, space: colorSpace, bitmapInfo: bitmapInfo),
                let ptr = context.data?.assumingMemoryBound(to: UInt8.self) else {
                    return self
            }
            
            context.draw(self.cgImage!, in: CGRect(x: 0, y: 0, width: width, height: height))
            
            var minX = width
            var minY = height
            var maxX: Int = 0
            var maxY: Int = 0
            
            for x in 1 ..< width {
                for y in 1 ..< height {
                    
                    let i = bytesPerRow * Int(y) + bytesPerPixel * Int(x)
                    let a = CGFloat(ptr[i + 3]) / 255.0
                    
                    if(a>0.15) {
                        if (x < minX) { minX = x };
                        if (x > maxX) { maxX = x };
                        if (y < minY) { minY = y};
                        if (y > maxY) { maxY = y};
                    }
                }
            }
            
            let rect = CGRect(x: CGFloat(minX),y: CGFloat(minY), width: CGFloat(maxX-minX), height: CGFloat(maxY-minY))
            let imageScale:CGFloat = self.scale
            let croppedImage =  self.cgImage!.cropping(to: rect)!
            let ret = UIImage(cgImage: croppedImage, scale: imageScale, orientation: self.imageOrientation)
            
            return ret;
        }
  
}
