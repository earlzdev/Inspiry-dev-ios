//
//  ImagePickerUI.swift
//  iosApp
//
//  Created by rst10h on 4.02.22.
//

import SwiftUI
import UIKit
import UniformTypeIdentifiers
import MobileCoreServices
import shared

struct ImagePickerUI: UIViewControllerRepresentable {
   
    @Binding var mediaResult: [PickMediaResult]
    @Binding var isActive: Bool
    
    func makeCoordinator() -> ImagePickerViewCoordinator {
        return ImagePickerViewCoordinator(mediaResult: $mediaResult, isActive: $isActive)
    }
    
    func makeUIViewController(context: Context) -> UIImagePickerController {
        let controller = UIImagePickerController()
        controller.sourceType = .photoLibrary
        controller.mediaTypes = UIImagePickerController.availableMediaTypes(for: .photoLibrary) ?? []
        controller.allowsEditing = true

        controller.delegate = context.coordinator
        return controller
    }
    
    func updateUIViewController(_ uiViewController: UIImagePickerController, context: Context) {
    }
    
}

class ImagePickerViewCoordinator: NSObject, UINavigationControllerDelegate, UIImagePickerControllerDelegate {
    
    static let typeImage = "public.image"
    static let typeVideo = "public.movie"
    
    @Binding var mediaResult: [PickMediaResult]
    @Binding var isActive: Bool
    
    init(mediaResult: Binding<[PickMediaResult]>, isActive: Binding<Bool>) {
        self._mediaResult = mediaResult
        self._isActive = isActive
    }
    
    func imagePickerController(_ picker: UIImagePickerController, didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey : Any]) {
        if let url = (info[UIImagePickerController.InfoKey.mediaURL] ?? info[UIImagePickerController.InfoKey.imageURL]) as? URL {
            let mediaType = info[UIImagePickerController.InfoKey.mediaType] as? String
            if (Dependencies.isDebug()) {
                if (mediaType != Self.typeImage && mediaType != Self.typeVideo) {
                    fatalError("unsupport media type: \(mediaType)")
                }
            }
            let uri = url.absoluteString
            let type: PickedMediaType = mediaType == Self.typeImage ? .media : .video
                       
            self.mediaResult = [PickMediaResult(uri: uri, type: type, size: Size(width: 500, height: 500))]
            
        }
        
        self.isActive = false
    }
    
    func imagePickerControllerDidCancel(_ picker: UIImagePickerController) {
        self.isActive = false
    }
    
}


