//
//  AppsflyerAnalyticsManagerApple.swift
//  iosApp
//
//  Created by rst10h on 4.01.23.
//

import Foundation

import Foundation
import shared
import AppsFlyerLib

class AppsflyerAnalyticsManagerImpl: AppsflyerAnalyticsManager {
    
    override func sendEvent(eventName: String, outOfSession: Bool, createParams: ((KotlinMutableDictionary<NSString, AnyObject>) -> Void)? = nil) {
        if let createParams = createParams {
            
            let dict = KotlinMutableDictionary<NSString, AnyObject>()
            createParams(dict)
            
            var toPass = [String: Any]()
            
            dict.forEach { (key: Any, value: Any?) in
                
                let key = key as? String
                if key != nil {
                    toPass[key!] = value
                }
            }
            print("send appsflyer event \(eventName)")
            AppsFlyerLib.shared().logEvent(name: eventName, values: toPass)
        } else {
            print("send appsflyer event \(eventName)")
            AppsFlyerLib.shared().logEvent(name: eventName, values: nil)
        }
    }
    
    override func setUserProperty(name: String, value: String) {
        //todo userproperty ignored?
    }
}
