//
//  RemoveBgProcessorImpl.swift
//  iosApp
//
//  Created by rst10h on 29.08.22.
//

import Foundation
import UIKit

extension UIImage {
    func scaled(by scale: CGFloat) -> UIImage {
        guard
            let scaledImage = CIImage(image: self)?.scaled(by: scale)
        else {
            return self
        }
        return UIImage(ciImage: scaledImage, scale: UIScreen.main.scale, orientation: imageOrientation)
    }
    
    func scaled(maxDimensions: CGSize) -> CGFloat {
        let scale = min(maxDimensions.width / size.width, maxDimensions.height / size.height)
        if scale >= 1 {
            return 1
        }
        return scale
    }
}

extension CIImage {
 
    func scaled(by scale: CGFloat) -> CIImage {
        guard scale != 1 else { return self }
        return applyingFilter("CILanczosScaleTransform", parameters: [kCIInputScaleKey: Float(scale),
                                                                      kCIInputAspectRatioKey: 1])
    }
}
