//
//  GoogleAnalyticsManager.swift
//  iosApp
//
//  Created by vlad on 10/7/21.
//

import Foundation
import shared
import Firebase

class GoogleAnalyticsManagerImpl: GoogleAnalyticsManager {
    
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

            Analytics.logEvent(eventName, parameters: toPass)
        } else {
            Analytics.logEvent(eventName, parameters: nil)
        }
    }
    
    override func setUserProperty(name: String, value: String) {
        Analytics.setUserProperty(name, forName: value)
    }
}
