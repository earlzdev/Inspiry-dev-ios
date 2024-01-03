//
//  RemoveBgProcessorImpl.swift
//  iosApp
//
//  Created by rst10h on 29.08.22.
//

import Foundation
import CoreGraphics
import CoreImage
import Kingfisher
import UIKit
import shared

class RemoveBGProcessorImplApple: RemoveBgProcessor {
    
    func determineFormat(originalFile: String) -> RemoveBgProcessorFormat {
        fatalError("not implemented!")
    }
    
    //returns size of image
    func getSizeOfExistingFile(file: String) -> Size {
        let home = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first!
        let filename = "remove-bg/\(file.getFileName()).png" //todo
        let url = home.appendingPathComponent(filename)
        do {
            let imageData = try Data(contentsOf: url)
            if let image = UIImage(data: imageData) {
                return image.size.sharedSize

            } else {
                print("The content of the URL cannot be handled as an image")
                return Size(width: 0,height: 0)
            }
        } catch {
            print(error)
        }
            
        return CGSize.zero.sharedSize
    }
    
    private func createDataBody(image: UIImage, fileName: String, uuid: String) -> Data {
        
        let lineBreak = "\r\n"
        var body = Data()
        body.appendString("--\(uuid + lineBreak)")
        body.appendString("Content-Disposition: form-data; name=\"image_file\"; filename=\"\(fileName)\"\(lineBreak)")
        body.appendString("Content-Type: \("image/jpg" + lineBreak + lineBreak)")
        body.append(image.jpegData(compressionQuality: 0.9)!)
        body.appendString(lineBreak)
        body.appendString("--\(uuid)--\(lineBreak)")
        
        return body
    }
    
    private let apiKey: String = "your-photoroom-apiKey"
    private let hostURL = URL(string: "https://sdk.photoroom.com/v1/segment")!
    
    func removeBg(originalFile: String, saveToFile: String, completionHandler: @escaping (Size?, Error?) -> Void) {
        
        KingfisherManager.shared.retrieveImage(with: URL(string: originalFile)!, options: nil, progressBlock: nil) { result in
            switch result {
            case .success(let value):
                self.removeBGProcessing(image: value.image, savePath: saveToFile, completionHandler: completionHandler)
            case .failure(let error):
                print("failed to retrieve image \(error) from ///// url = \(originalFile) //// saved file \(saveToFile)")
                completionHandler(nil, RemoveBGError.invalidData)
            }
        }
    }
    
    private func removeBGProcessing(image: UIImage, savePath: String, completionHandler: @escaping (Size?, Error?) -> Void) {
        
        var request = URLRequest(url: hostURL)
        request.httpMethod = "POST"
        request.timeoutInterval = 30.0
        
        let scale = image.scaled(maxDimensions: CGSize(width: 1600, height: 1600))
        let scaledImage = image.scaled(by: scale)
        let uuid = NSUUID().uuidString
        let requestBody = createDataBody(image: scaledImage, fileName: savePath.getFileName(), uuid: uuid)
        
        request.httpBody = requestBody
        
        
        
        request.addValue("multipart/form-data; boundary=\(uuid)", forHTTPHeaderField: "Content-Type")
        request.addValue(apiKey, forHTTPHeaderField: "X-Api-Key")
        
        let config = URLSessionConfiguration.default
        let session = URLSession(configuration: config)
        let task = session.dataTask(with: request, completionHandler: { responseData, response, error in
            guard let responseData = responseData,
                  let response = response as? HTTPURLResponse, error == nil else {
                completionHandler(nil, RemoveBGError.serverError)
                return
            }
            
            guard (200 ... 299) ~= response.statusCode else {
                print("statusCode should be 2xx, but is \(response.statusCode)")
                print("response = \(response)")
                completionHandler(nil, RemoveBGError.serverError)
                return
            }
            
            
            print("crop alpha..")
            DispatchQueue.global().async {
                guard let imageData = UIImage(data: responseData)?.cropAlpha() else {
                    print("Error decoding server response")
                    completionHandler(nil, RemoveBGError.serverError)
                    return
                }
                print("save image.. size = \(imageData.size.sharedSize)")
                let saved = self.saveToFile(image: imageData, path: savePath)
                
                completionHandler(saved ? imageData.size.sharedSize : nil, saved ? nil : RemoveBGError.fileIOError)
            }
            
            
        })
        task.resume()
        
    }
    
    private func saveToFile( image: UIImage, path: String) -> Bool {
        do {
            let fileURL = URL(string: "file://\(path)")!
            if let data = image.pngData(),
               !FileManager.default.fileExists(atPath: fileURL.path) {
                try data.write(to: fileURL)
                return true
            } else {
                print("file is not saved, it may already exist")
                return false
            }
        } catch {
            print("error:", error)
            return false
        }
    }
}

extension Data {
    mutating func appendString(_ string: String) {
        let data = string.data(using: .utf8)
        append(data!)
    }
}

enum RemoveBGError: Error {
    case unknown
    case serverError
    case invalidData
    case fileIOError
}

extension RemoveBGError: LocalizedError {
    public var errorDescription: String? {
        switch self {
        case .invalidData:
            return NSLocalizedString("Data of image is not valid.", comment: "")
        case .serverError:
            return NSLocalizedString("There was a server error.", comment: "")
        case .unknown:
            return NSLocalizedString("unsupported error", comment: "")
        case .fileIOError:
            return NSLocalizedString("Image not saved in Documents directory", comment: "")
        }
    }
}
