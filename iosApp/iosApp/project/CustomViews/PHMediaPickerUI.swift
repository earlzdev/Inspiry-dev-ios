//
//  PHMediaPickerUI.swift
//  iosApp
//
//  Created by rst10h on 20.07.22.
//

import SwiftUI
import UIKit
import UniformTypeIdentifiers
import MobileCoreServices
import PhotosUI
import shared

struct MediaPickerUI: UIViewControllerRepresentable {
    
    @Binding var mediaResult: [PickMediaResult]
    @Binding var isActive: Bool
    @Binding var isLoading: Bool
    @Binding var iCloudProgress: Double?
    let maxMediasCount: Int
    
    func makeCoordinator() -> MediaPickerCoordinator {
        return MediaPickerCoordinator(mediaResult: $mediaResult, isActive: $isActive, isLoading: $isLoading, iCloudProgress: $iCloudProgress)
    }
    
    func makeUIViewController(context: Context) -> PHPickerViewController {
        var configuration: PHPickerConfiguration = PHPickerConfiguration(photoLibrary: .shared())
        configuration.selectionLimit = maxMediasCount
        let controller = PHPickerViewController(configuration: configuration)
        controller.delegate = context.coordinator
        return controller
    }
    
    func updateUIViewController(_ uiViewController: PHPickerViewController, context: Context) {
    }
    
}

class MediaPickerCoordinator: NSObject, UINavigationControllerDelegate, PHPickerViewControllerDelegate {
    
    static let typeImage = "public.image"
    static let typeVideo = "public.movie"
    
    @Binding var mediaResult: [PickMediaResult]
    @Binding var isActive: Bool
    @Binding var isLoading: Bool
    @Binding var iCloudProgress: Double?
    
    private var pmr: [PickMediaResult] = []
    
    init(mediaResult: Binding<[PickMediaResult]>, isActive: Binding<Bool>, isLoading: Binding<Bool>, iCloudProgress: Binding<Double?>) {
        self._mediaResult = mediaResult
        self._isActive = isActive
        self._isLoading = isLoading
        self._iCloudProgress = iCloudProgress
    }
    
    func picker(_ picker: PHPickerViewController, didFinishPicking results: [PHPickerResult]) {
        guard isLoading == false else { return } //ignore extra images (there is a bug in PHMediaPicker that allows you to select more images than expected)
        
        let selectedCount = results.count
        if (selectedCount > 0) {
            isLoading = true
        }
        var loaded = 0 {
            didSet {
                if (loaded >= selectedCount) {
                    DispatchQueue.main.async { [self] in
                        mediaResult = pmr
                        isLoading = false
                    }
                }
            }
        }
        
        func processURL(phAsset: PHAsset) {
            phAsset.getURL { url in
                var type: PickedMediaType  = phAsset.mediaType == .video ? .video : .media
                self.pmr.append(
                    PickMediaResult(uri: url!.absoluteString, type: type, size: Size(width: phAsset.pixelWidth.int32, height: phAsset.pixelHeight.int32))
                )
                loaded += 1
            }
        }
        
        results.forEach { res in
            iCloudProgress = nil
            if let phAsset = PHAsset.fetchAssets(withLocalIdentifiers: [res.assetIdentifier!], options: nil).firstObject {
                phAsset.checkIsCloud(icloudProgress: { progress in
                    DispatchQueue.main.async {
                        self.iCloudProgress = progress
                    }
                }) { isInCloud in
                    print("in icloud \(isInCloud)")
                    if (isInCloud) {
                        res.itemProvider.loadObject(ofClass: UIImage.self, completionHandler: { _, _ in //download media from icloud to library. We need only this process without any objects
                            processURL(phAsset: phAsset)
                        })
                    } else {
                        processURL(phAsset: phAsset)
                    }
                }
            } else {
                loaded += 1
            }
            
        }
        
        DispatchQueue.main.async {
            print("hide dialog")
            self.hideDialog()
        }
    }
    
    private func hideDialog() {
        isActive = false
    }
    
    
}



extension PHAsset {
    
    func checkIsCloud( icloudProgress: @escaping (Double) -> Void, resultHandler: @escaping (Bool) -> ())  {
        let cachingImageManager = PHCachingImageManager()
        if self.mediaType == .video {
            let options = PHVideoRequestOptions()
            options.deliveryMode = .highQualityFormat
            options.isNetworkAccessAllowed = true
            options.progressHandler = { progress, error, _, _ in //todo if error??
                icloudProgress(progress)
            }
            
            cachingImageManager.requestAVAsset(forVideo: self, options: options, resultHandler: { avAsset, audioMix, info in
                DispatchQueue.main.async(execute: {
                    if info?["PHImageFileSandboxExtensionTokenKey"] != nil {
                        resultHandler(false)
                    } else if (info?[PHImageResultIsInCloudKey] as? NSNumber)?.boolValue ?? false {
                        resultHandler(true)
                    } else {
                        resultHandler(false)
                    }
                })
            })
            
        } else {
            cachingImageManager.requestImageData(for: self, options: nil, resultHandler: { imageData, dataUTI, orientation, info in
                
                DispatchQueue.main.async(execute: {
                    if (info?[PHImageResultIsInCloudKey] as? NSNumber)?.boolValue ?? false {
                        resultHandler(true)
                    } else {
                        resultHandler(false)
                    }
                })
            })
        }
    }
    
    func getURL(completionHandler : @escaping ((_ responseURL : URL?) -> Void)){
        if self.mediaType == .image {
            let options: PHContentEditingInputRequestOptions = PHContentEditingInputRequestOptions()
            options.canHandleAdjustmentData = {(adjustmeta: PHAdjustmentData) -> Bool in
                return true
            }
            self.requestContentEditingInput(with: options, completionHandler: {(contentEditingInput: PHContentEditingInput?, info: [AnyHashable : Any]) -> Void in
                completionHandler(contentEditingInput!.fullSizeImageURL as URL?)
            })
        } else if self.mediaType == .video {
            let options: PHVideoRequestOptions = PHVideoRequestOptions()
            options.version = .original
            PHImageManager.default().requestAVAsset(forVideo: self, options: options, resultHandler: {(asset: AVAsset?, audioMix: AVAudioMix?, info: [AnyHashable : Any]?) -> Void in
                if let urlAsset = asset as? AVURLAsset {
                    let localVideoUrl: URL = urlAsset.url as URL
                    completionHandler(localVideoUrl)
                } else {
                    completionHandler(nil)
                }
            })
        }
    }
}
