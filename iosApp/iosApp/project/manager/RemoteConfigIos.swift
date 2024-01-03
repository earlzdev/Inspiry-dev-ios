//
//  RemoteConfigIos.swift
//  MusicFeatureIos
//
//  Created by vlad on 18/4/21.
//

import Foundation
import Firebase
import shared

class RemoteConfigIos: InspRemoteConfig {
    
    let remoteConfig = RemoteConfig.remoteConfig()
    
    override init() {
        super.init()
        
        let settings = RemoteConfigSettings()
        settings.minimumFetchInterval = Double(self.minimumFetchInterval)
        remoteConfig.setDefaults(fromPlist: "RemoteConfigDefaults")
        remoteConfig.configSettings = settings
        
        remoteConfig.fetchAndActivate(completionHandler: { status, error in
            // print("firebasRemoteConfig, statis \(status), error \(error)")
        })
    }
    
    override func getBoolean(key: String) -> Bool {
        return remoteConfig.configValue(forKey: key).boolValue
    }
    
    override func getDouble(key: String) -> Double {
        return remoteConfig.configValue(forKey: key).numberValue as! Double
    }
    
    override func getLong(key: String) -> Int64 {
        return remoteConfig.configValue(forKey: key).numberValue as! Int64
    }
    
    override func getString(key: String) -> String {
        return remoteConfig.configValue(forKey: key).stringValue!
    }
    
    
}
