//
//  FacebookAnalyticsManagerImpl.swift
//  iosApp
//
//  Created by vlad on 10/7/21.
//

import Foundation
import shared
import FacebookCore

class FacebookAnalyticsManagerImpl: FacebookAnalyticsManager {
    override func sendFacebookViewContentEvent(name: String) {
        print("facebook event \(name)")
        AppEvents.shared.logEvent(AppEvents.Name(rawValue: name))
    }
    
//    override func sendEvent(eventName: String, outOfSession: Bool, createParams: ((KotlinMutableDictionary<NSString, AnyObject>) -> Void)? = nil) {
//
//        if let createParams = createParams {
//
//            let dict = KotlinMutableDictionary<NSString, AnyObject>()
//            createParams(dict)
//
//            var toPass = [AppEvents.ParameterName: Any]()
//            dict.forEach { (key: Any, value: Any?) in
//
//                if let key = key as? String {
//                    let key = AppEvents.ParameterName(rawValue: key)
//                    toPass[key] = value
//                }
//            }
//
//            //AppEvents.shared.logEvent(eventName: AppEvents.Name(rawValue: eventName), withEventProperties: toPass)
//            AppEvents.shared.logEvent(AppEvents.Name(rawValue: eventName), parameters: toPass)
//            print("facebook event \(eventName)")
//        } else {
//            AppEvents.shared.logEvent(AppEvents.Name(rawValue: eventName))
//            print("facebook event \(eventName)")
//        }
//    }
//
//    override func setUserProperty(name: String, value: String) {
//        print("facebook userProp \(name)")
////        AppEvents.shared.setUserData(name, forType: .externalId) //todo
//    }
    
}
