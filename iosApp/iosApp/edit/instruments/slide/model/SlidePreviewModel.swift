//
//  SlidePreviewModel.swift
//  iosApp
//
//  Created by rst10h on 7.10.22.
//

import Foundation
import UIKit
import AVFoundation
import CoreMedia
import Kingfisher

class SlidePreviewModel: ObservableObject {
    let url: URL?
    
    @Published
    var preview: UIImage? = nil
    
    init(url: URL?) {
        
        self.url = url
        guard let url = url else { return }
        
        DispatchQueue.main.async {
            if let image = self.extractFirstFrame(url: url) {
                self.preview = image
                KingfisherManager.shared.cache.store(image, forKey: url.absoluteString, options: KingfisherParsedOptionsInfo([.backgroundDecode, .diskCacheExpiration(.never)]))
            } else {
                KingfisherManager.shared.retrieveImage(with: url, options: nil, progressBlock: nil, completionHandler: { result in
                    switch result {
                    case .success(let value):
                        self.preview = value.image
                    case .failure(let error):
                        print("Error loading image (SldePreview): \(error)")
                    }
                })
            }
        }
    }
    
    func extractFirstFrame(url: URL) -> UIImage?
    {
        do {
            guard let typeID = try url.resourceValues(forKeys: [.typeIdentifierKey]).typeIdentifier else {
                print("resource not found url \(url)")
                return nil
                
            }
            print ("type id = \(typeID)")
            guard let supertypes = UTType(typeID)?.supertypes else { return nil}
            var isVideo = false
            supertypes.forEach { type in
                print("url type \(type)")
                if (type.conforms(to: .movie)) {
                    isVideo = true
                }
            }
            print("is video \(isVideo) url \(url)")
            if isVideo {
                let asset = AVAsset(url: url)
                let generator = AVAssetImageGenerator.init(asset: asset)
                generator.appliesPreferredTrackTransform = true
                let time = CMTime(value: 0, timescale: 1)
                guard let cgImage = try? generator.copyCGImage(at: time, actualTime: nil) else { return nil}
                return UIImage(cgImage: cgImage)
            }

        } catch {
            print(error.localizedDescription)
        }
        return nil
    }
}

extension UIImage {
    func rotate(radians: Float) -> UIImage? {
        var newSize = CGRect(origin: CGPoint.zero, size: self.size).applying(CGAffineTransform(rotationAngle: CGFloat(radians))).size

        newSize.width = floor(newSize.width)
        newSize.height = floor(newSize.height)

        UIGraphicsBeginImageContextWithOptions(newSize, false, self.scale)
        let context = UIGraphicsGetCurrentContext()!

        context.translateBy(x: newSize.width/2, y: newSize.height/2)
        context.rotate(by: CGFloat(radians))
        self.draw(in: CGRect(x: -self.size.width/2, y: -self.size.height/2, width: self.size.width, height: self.size.height))
        let newImage = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()

        return newImage
    }
}
