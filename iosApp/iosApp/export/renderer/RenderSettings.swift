//
//  RenderSettings.swift
//  iosApp
//
//  Created by rst10h on 4.08.22.
//

import AVFoundation
import UIKit
import Photos

struct RenderSettings {
    
    var size : CGSize = .zero
    var fps: Int32 = 30   // frames per second
    var avCodecKey = AVVideoCodecType.h264
    var videoFilenameExt = "mp4"
    var tempName = "testRender"
    
    var tempURL: URL {
        return Self.getTempUrl(filename: tempName, fileExtension: videoFilenameExt, clear: true)
    }
    var tempMixedURL: URL {
        return Self.getTempUrl(filename: tempName + "_mixed", fileExtension: videoFilenameExt, clear: true)
    }
    
    func clearTempFiles() {
        removeFileAtURL(fileURL: tempMixedURL)
        removeFileAtURL(fileURL: tempURL)
    }
    
    static func getTempUrl(filename: String, fileExtension: String, clear: Bool) -> URL {
        
        let fileManager = FileManager.default
        if let tmpDirURL = try? fileManager.url(for: .cachesDirectory, in: .userDomainMask, appropriateFor: nil, create: true) {
            let url = tmpDirURL.appendingPathComponent(filename).appendingPathExtension(fileExtension)
            return url
        }
        fatalError("can't create temp file \(filename)")
    }
    
    static func clearCache(){
        let fileManager = FileManager.default
        guard let tmpDirURL = try? fileManager.url(for: .cachesDirectory, in: .userDomainMask, appropriateFor: nil, create: false) else { return }
        do {
            // Get the directory contents urls (including subfolders urls)
            let directoryContents = try fileManager.contentsOfDirectory( at: tmpDirURL, includingPropertiesForKeys: nil, options: [])
            for file in directoryContents {
                if (!file.isDirectory) {
                    do {
                        debugPrint("removing temporary file \(file.absoluteString)")
                        print("removing temporary file \(file.absoluteString)")
                        try fileManager.removeItem(at: file)
                    }
                    catch let error as NSError {
                        debugPrint("Ooops! Something went wrong: \(error)")
                    }
                }
                
            }
        } catch let error as NSError {
            print(error.localizedDescription)
        }
    }
    
    private func removeFileAtURL(fileURL: URL) {
        do {
            try FileManager.default.removeItem(atPath: fileURL.path)
        }
        catch _ as NSError {
            // Assume file doesn't exist.
        }
    }
}
