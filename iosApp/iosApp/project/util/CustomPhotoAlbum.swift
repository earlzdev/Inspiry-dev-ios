//
//  CustomPhotoAlbum.swift
//  iosApp
//
//  Created by rst10h on 3.08.22.
//

import Foundation
import Photos
import UIKit

class CustomPhotoAlbum: NSObject {
    static let albumName = "Inspiry"
    static let sharedInstance = CustomPhotoAlbum()
    
    var assetCollection: PHAssetCollection!
    var isAuthorized: Bool = false
    override init() {
        super.init()
        
        if let assetCollection = fetchAssetCollectionForAlbum() {
            self.assetCollection = assetCollection
            return
        }
        PHPhotoLibrary.requestAuthorization(for: .readWrite, handler: requestAuthorizationHandler)
    }
    
    func requestAuthorizationHandler(status: PHAuthorizationStatus) {
        if PHPhotoLibrary.authorizationStatus() == PHAuthorizationStatus.authorized {
            // ideally this ensures the creation of the photo album even if authorization wasn't prompted till after init was done
            print("trying again to create the album")
            isAuthorized = true
            self.createAlbum()
        } else {
            isAuthorized = false
            print("should really prompt the user to let them know it's failed")
        }
    }
    
    func createAlbum() {
        PHPhotoLibrary.shared().performChanges({
            PHAssetCollectionChangeRequest.creationRequestForAssetCollection(withTitle: CustomPhotoAlbum.albumName)   // create an asset collection with the album name
        }) { success, error in
            if success {
                self.assetCollection = self.fetchAssetCollectionForAlbum()
            } else {
                print("error \(String(describing: error))")
            }
        }
    }
    
    func fetchAssetCollectionForAlbum() -> PHAssetCollection? {
        let fetchOptions = PHFetchOptions()
        fetchOptions.predicate = NSPredicate(format: "title = %@", CustomPhotoAlbum.albumName)
        let collection = PHAssetCollection.fetchAssetCollections(with: .album, subtype: .any, options: fetchOptions)
        
        if let _: AnyObject = collection.firstObject {
            return collection.firstObject
        }
        return nil
    }
    
    func save(_ image: UIImage, filename: String, onSuccess: @escaping (_ localId: String?, _ url: URL?) -> ()) {
        if assetCollection == nil {
            // if there was an error upstream, skip the save
            onSuccess(nil, nil)
            return
        }
        PHPhotoLibrary.requestAuthorization { status in
            
            if status != .authorized {
                onSuccess (nil, nil)
                return
            }
            var placeholder: PHObjectPlaceholder? = nil
            PHPhotoLibrary.shared().performChanges({
                let options = PHAssetResourceCreationOptions()
                options.originalFilename = filename
                let newcreation:PHAssetCreationRequest = PHAssetCreationRequest.forAsset()
                newcreation.addResource(with: PHAssetResourceType.photo, data: image.jpegData(compressionQuality: 1)!, options: options)
                
                placeholder = newcreation.placeholderForCreatedAsset
                let albumChangeRequest = PHAssetCollectionChangeRequest(for: self.assetCollection)
                let enumeration: NSArray = [placeholder!]
                albumChangeRequest!.addAssets(enumeration)
                
            }) { success, error in
                if !success {
                    print("Could not save photo to photo library: \(error)")
                } else {
                    let localId = placeholder?.localIdentifier
                    let assetResult = PHAsset.fetchAssets(withLocalIdentifiers: [localId].compactMap { $0 }, options: nil) as? PHFetchResult
                    let asset = assetResult?.firstObject as? PHAsset
                    //todo exception Handling
                    asset?.getURL { url in
                        if let url = url {
                            onSuccess(localId!, url)
                        }
                    }
                    
                }
            }
        }
    }
    
    func saveToLibrary(videoURL: URL, onResult: @escaping (_ localId: String?, _ url: URL?) -> Void) {
        PHPhotoLibrary.requestAuthorization { status in
            
            if status != .authorized {
                onResult (nil, videoURL)
                return
            }
            var placeholder: PHObjectPlaceholder? = nil
            PHPhotoLibrary.shared().performChanges({
                let assetChangeRequest = PHAssetChangeRequest.creationRequestForAssetFromVideo(atFileURL: videoURL)!
                placeholder = assetChangeRequest.placeholderForCreatedAsset
                let albumChangeRequest = PHAssetCollectionChangeRequest(for: self.assetCollection)
                let enumeration: NSArray = [placeholder!]
                albumChangeRequest!.addAssets(enumeration)
            }) { [weak self] success, error in
                if !success {
                    onResult(nil, nil)
                    print("Could not save video \(videoURL) to photo library: \(error)")

                } else {
                    let localId = placeholder?.localIdentifier
                    let assetResult = PHAsset.fetchAssets(withLocalIdentifiers: [localId].compactMap { $0 }, options: nil) as? PHFetchResult
                    let asset = assetResult?.firstObject as? PHAsset
                    //todo exception Handling
                    asset?.getURL { url in
                        if let url = url {
                            onResult(localId!, url)
                        }
                    }
                    if (asset == nil) {
                        onResult(nil, videoURL)
                    }
                    
                }
            }
        }
    }
    
    func getLastVideoURL(complectionHandler: @escaping (URL) -> Void) {
        let fetchOptions = PHFetchOptions()
        fetchOptions.sortDescriptors = [NSSortDescriptor(key:"creationDate", ascending: false)]
        fetchOptions.fetchLimit = 1
        let assetResult: PHFetchResult = PHAsset.fetchAssets(with: .video, options: fetchOptions)
        assetResult.firstObject?.getURL { url in
            if let url = url {
                complectionHandler(url)
            }
        }
        
        
    }
    
    
}
