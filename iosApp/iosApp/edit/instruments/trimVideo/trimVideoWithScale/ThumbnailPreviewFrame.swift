//
//  ThumbnailPreviewFrame.swift
//  iosApp
//
//  Created by rst10h on 18.10.22.
//

import AVFoundation
import UIKit

class ThumbnailPreviewFrame: UIImageView {
    var onFrameUpdated: ((_ bounds: CGRect) -> Void)?
    
    override func layoutSubviews() {
        super.layoutSubviews()
        onFrameUpdated?(self.bounds)
    }
      
    private let generator: AVAssetImageGenerator
    private let time: CMTime
    
    init(generator: AVAssetImageGenerator, time: CMTime) {
        
        self.generator = generator
        self.time = time
        
        super.init(frame: .zero)
        
        contentMode = .scaleAspectFill
        clipsToBounds = true

    }
    
    func showFrame() {
        image = thumbnailFromVideo(generator: generator, time: time)
    }
    
    func releaseFrame() {
        image = nil
    }
    
    
    private func thumbnailFromVideo(generator: AVAssetImageGenerator, time: CMTime) -> UIImage{
        do {
            let cgImage = try generator.copyCGImage(at: time, actualTime: nil)
            let uiImage = UIImage(cgImage: cgImage)
            return uiImage
        } catch {
            
        }
        return UIImage()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func isVisible() -> Bool {
        if isHidden || superview == nil {
            return false
        }
        
        if let rootViewController = UIApplication.shared.keyWindow?.rootViewController,
           let rootView = rootViewController.view {
            
            let viewFrame = self.convert(self.bounds, to: rootView)
            
            let topSafeArea: CGFloat
            let bottomSafeArea: CGFloat
            
            topSafeArea = rootView.safeAreaInsets.top
            bottomSafeArea = rootView.safeAreaInsets.bottom
            
            return viewFrame.minX >= 0 &&
            viewFrame.maxX <= rootView.bounds.width &&
            viewFrame.minY >= topSafeArea &&
            viewFrame.maxY <= rootView.bounds.height - bottomSafeArea
        }
        
        return false
    }
    
}
