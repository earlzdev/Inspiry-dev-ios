//
//  FileManagerTest.swift
//  MusicFeatureIos
//
//  Created by vlad on 18/4/21.
//

import Foundation
import shared

class JsonCacheClientImpl: JsonCacheClient {
    
    func cacheLastModifTime(path: String) -> Int64 {
        do {
            
            let attrib = try FileManager.default.attributesOfItem(atPath: getFilePath(innerPath: path))
            
            let date = attrib[FileAttributeKey.creationDate] as? Date?
            
            let timeInterval = date??.timeIntervalSince1970 as Double?
            
            return timeInterval == nil ? 0 : Int64(timeInterval! * 1000)
            
        } catch {
            
            return 0
        }
    }
    
    func deleteCache(path: String) {
        do {
            try FileManager.default.removeItem(atPath: getFilePath(innerPath: path))
        } catch {
            
        }
    }
    
    func readCache(path: String) throws -> String {
        
        let finalPath = getFilePath(innerPath: path)
        let data = FileManager.default.contents(atPath: finalPath)
        if data == nil {
            throw NSError(domain: "readCache is empty, finalPath \(finalPath)", code: 42, userInfo: ["ui1":12, "ui2":"val2"] )
        }
        return String(decoding: data!, as: UTF8.self)
    }
    
    func saveCache(path: String, cache: String) throws {
        let finalPath = getFinalUrl(innerPath: path)
        
        // deleteCache(path: path)
        
        let data = Data(cache.utf8)
        
        try data.write(to: finalPath)
    }
    
    
    private func pathForCacheDirectoryAsString() -> String? {
        return NSSearchPathForDirectoriesInDomains(.cachesDirectory, .userDomainMask, true).first
    }
    private func pathForCacheDirectoryAsURL() -> URL? {
        return FileManager.default.urls(for: .cachesDirectory, in: FileManager.SearchPathDomainMask.userDomainMask).first
    }
    
    private func getFinalUrl(innerPath: String) -> URL {
        
        var root = pathForCacheDirectoryAsURL()
        root?.appendPathComponent(innerPath)
        
        let folder = root?.deletingLastPathComponent()
        do {
            try FileManager.default.createDirectory(at: folder!, withIntermediateDirectories: true, attributes: nil)
        } catch {
            print(error.localizedDescription)
        }
        
        return root!
    }
    
    private func getFilePath(innerPath: String) -> String {
        let root = pathForCacheDirectoryAsString()!
        
        let result = "\(root)/\(innerPath)"
        
        return result
    }
    
    static func testCache() {
        do {
            
            let cacheManager = JsonCacheClientImpl()
            
            try cacheManager.saveCache(path: "test/file.json", cache: "first cache string")
            
            var readCacheResult = try cacheManager.readCache(path: "test/file.json")
            
            print("readCacheResult \(readCacheResult)")
            
            let lastModifTime = cacheManager.cacheLastModifTime(path: "test/file.json")
            
            print("cacheLastModifTime \(lastModifTime)")
            
            cacheManager.deleteCache(path: "test/file.json")
            
            
            readCacheResult = try cacheManager.readCache(path: "test/file.json")
            
            print("readCacheResult \(readCacheResult)")
            
            
        } catch {
            print("Cache error: \(error).")
        }
    }
}
