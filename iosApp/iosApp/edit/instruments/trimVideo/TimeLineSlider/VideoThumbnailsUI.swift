//
//  VideoThumbnailsUI.swift
//  iosApp
//
//  Created by rst10h on 27.07.22.
//

import AVFoundation
import UIKit

class VideoThumbnailsUI: UIView {
    
    static let leftOffset = ScalableTrimSlider.horizontalSpacing + ScalableTrimSlider.thumbnailsPadding
    
    private let screenWidth = UIScreen.screenWidth
    
    private var url: URL? = nil
    private var newUrl: URL? = nil
    
    private var thumbnailViews = [ThumbnailPreviewFrame]()
    
    private var lastWidth: Int = 0
       
    required init() {
        super.init(frame: .zero)
    }
    
    override func willMove(toWindow newWindow: UIWindow?) {
        if (newWindow == nil) {
            url = nil
        }
    }
    
    override func layoutSubviews() {
        guard self.bounds.size != .zero else { return }
        whenSizeKnownAction?()
    }
    
    override var frame: CGRect {
        didSet {
            print("update thumbs frame \(self.globalPoint)")
        }
    }
    
    private var whenSizeKnownAction: (() -> Void)? = nil
    
    private var asset: AVAsset?
    private var imgGenerator: AVAssetImageGenerator?
    
    func updateThumbnails(videoURL: URL) {
        
        if (whenSizeKnownAction == nil) {
            whenSizeKnownAction = {
                self.updateThumbnails(videoURL: videoURL)
            }
        }
        
        if (self.frame.size == .zero) {
            return
        }
        
        if asset == nil {
            asset = AVAsset(url: videoURL) as AVAsset
            imgGenerator = AVAssetImageGenerator(asset: asset!)
            imgGenerator?.appliesPreferredTrackTransform = true
            imgGenerator?.maximumSize = CGSizeMake(80, 80)
        }
        guard let imgGenerator = imgGenerator else { return }
        
        let frameWidth = Int(self.frame.width)
        let sub: Int = abs(frameWidth - lastWidth)
        
        
        if (self.url == videoURL && sub < 3) {
            
            return
        }
        
        print("update thumbs width \(self.frame.width) last \(self.lastWidth)")
        
        self.url = videoURL
        self.lastWidth = Int(self.frame.width)
        
        var offset: Float64 = 0
        let duration = videoURL.videoDurationSeconds()
        
        let imagesCount = thumbnailCount()
        
        DispatchQueue.main.async { [self] in
            for view in self.thumbnailViews{
                view.removeFromSuperview()
            }
            self.thumbnailViews.removeAll()
            var xPos: CGFloat = 0.0
            var width: CGFloat = 0.0
            let visibleOffset = globalPoint?.x ?? 0
            print("visible offset \(visibleOffset)") 
            for i in 0..<imagesCount{
                let thumbnail = ThumbnailPreviewFrame(generator: imgGenerator, time: CMTimeMake(value: Int64(offset), timescale: 1))
                if xPos + self.frame.size.height < self.frame.width{
                    width = self.frame.size.height
                }else{
                    width = self.frame.size.width - xPos
                }
                
                thumbnail.frame = CGRect(x: xPos,
                                         y: 0.0,
                                         width: width,
                                         height: self.frame.size.height)
                
                
                
                self.addSubview(thumbnail)
                self.sendSubviewToBack(thumbnail)
                xPos = xPos + self.frame.size.height
                //if (xPos + visibleOffset - Self.leftOffset > 0 && xPos + visibleOffset - Self.leftOffset < screenWidth ) {
                DispatchQueue.main.async {
                    
                
                    thumbnail.showFrame()
                }
                
            }
            
            
        }
        
    }
    
    private func thumbnailCount() -> Int {
        
        var num : Double = 0;
        
        num = Double(self.frame.size.width) / Double(self.frame.size.height)
        
        
        return Int(ceil(num))
    }
    
    
    
    private func thumbnailFromVideo(videoUrl: URL, time: CMTime) -> UIImage{
        if asset == nil {
            asset = AVAsset(url: videoUrl) as AVAsset
            imgGenerator = AVAssetImageGenerator(asset: asset!)
            imgGenerator?.appliesPreferredTrackTransform = true
            imgGenerator?.maximumSize = CGSizeMake(80, 80)
        }
        
        guard
            let imgGenerator = imgGenerator
        else { return UIImage() }
        
        do {
            let cgImage = try imgGenerator.copyCGImage(at: time, actualTime: nil)
            let uiImage = UIImage(cgImage: cgImage)
            return uiImage
        } catch {
            
        }
        return UIImage()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    var globalPoint :CGPoint? {
        return self.superview?.convert(self.frame.origin, to: nil)
    }
    
    var globalFrame :CGRect? {
        return self.superview?.convert(self.frame, to: nil)
    }
    
}

extension URL {
    func videoDurationSeconds() -> Float64 {
        let source = AVURLAsset(url: self)
        return CMTimeGetSeconds(source.duration)
    }
}

