//
// Created by vlad on 14/9/21.
//

import Foundation
import shared

class FileReadWriteImpl: FileReadWrite {
    
    static let sharedInstance = FileReadWriteImpl()
    
    func readContentFromAssets(asset: AssetResource) -> String {
        return asset.readText()
    }
    
    func readContentFromAssets(path: String) -> String {
        let asset = ResourceContainerExtKt.getAssetByFilePath(MR.assets(), filePath: path)
        return readContentFromAssets(asset: asset)
    }
    
    func readContentFromFiles(path: String) -> String {
        
        guard let contentData = FileManager.default.contents(atPath: path) else {
            return ""
        }
        
        let content = String(data:contentData, encoding:String.Encoding.utf8) ?? ""
        
        return content
    }
    
    func writeContentToFile(str: String, path: String) {
        //writing
        do {
            try str.write(toFile: path, atomically: false, encoding: String.Encoding.utf8)
        }
        catch {/* error handling here */}
    }
}
