//
//  AmplitudeAnalyticsManagerImpl.swift
//  iosApp
//
//  Created by vlad on 10/7/21.
//

import Foundation
import shared
import Amplitude

class AmplitudeAnalyticsManagerImpl: AmplitudeAnalyticsManager {
   
    
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
            Amplitude.instance().logEvent(eventName, withEventProperties: toPass)
            print("amplitude event \(eventName)")
        } else {
            Amplitude.instance().logEvent(eventName)
            print("amplitude event \(eventName)")
        }
    }
    
    override func setUserProperty(name: String, value: String) {
        print("amplitude userProp \(name)")
        Amplitude.instance().setUserProperties([name: value])
    }
}
