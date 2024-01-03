//
//  AppodealAnalyticsManagerImpl.swift
//  iosApp
//
//  Created by rst10h on 13.01.23.
//

import Foundation
import shared

class AppodealAnalyticsManagerImpl: AppodealAnalyticsManager {

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

//            Analytics.logEvent(eventName, parameters: toPass)
        } else {
//            Analytics.logEvent(eventName, parameters: nil)
        }
    }
    
    override func setUserProperty(name: String, value: String) {
//        Analytics.setUserProperty(name, forName: value)
    }
}
