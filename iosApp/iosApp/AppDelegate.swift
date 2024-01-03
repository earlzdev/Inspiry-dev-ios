//
//  AppDelegate.swift
//  iosApp
//
//  Created by rst10h on 4.01.23.
//

import Foundation
import SwiftUI
import UIKit
import FacebookCore
import shared
import AppsFlyerLib
import AppTrackingTransparency

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        
        ApplicationDelegate.shared.application(
            application,
            didFinishLaunchingWithOptions: launchOptions
        )
        
        //FirebaseApp.configure()
        print("app init")
        
        //AppsFlyerLib.shared().isDebug = DebugManager().isDebug
        AppsFlyerLib.shared().appsFlyerDevKey = "your-appsfler-key"
        AppsFlyerLib.shared().appleAppID = "app.inspiry.stories"
        
        AppsFlyerLib.shared().delegate = self
        
        //        AppsFlyerLib.shared().start()
        requestTrackingAuthorization()
        NotificationCenter.default.addObserver(self,
                                               selector: #selector(didBecomeActiveNotification),
                                               name: UIApplication.didBecomeActiveNotification,
                                               object: nil)
        
        
        return true
    }
    @objc func didBecomeActiveNotification() {
        print("appsflyer started")
        AppsFlyerLib.shared().start()
    }
    
    private func requestTrackingAuthorization() { //need or not?
//        guard #available(iOS 14, *) else { return }
//        DispatchQueue.main.asyncAfter(deadline: .now() + 5.0, execute: {
//            ATTrackingManager.requestTrackingAuthorization { _ in
//                DispatchQueue.main.async { [weak self] in
//                    // self?.router.close() or nothing to do
//                }
//            }
//        })
    }
    
    func application(_ app: UIApplication, open url: URL, options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        
        ApplicationDelegate.shared.application(
            app,
            open: url,
            sourceApplication: options[UIApplication.OpenURLOptionsKey.sourceApplication] as? String,
            annotation: options[UIApplication.OpenURLOptionsKey.annotation]
        )
    }
}

extension AppDelegate: AppsFlyerLibDelegate {
    
    // Handle Organic/Non-organic installation
    func onConversionDataSuccess(_ data: [AnyHashable: Any]) {
        
        print("onConversionDataSuccess data:")
        for (key, value) in data {
            print(key, ":", value)
        }
        
        if let status = data["af_status"] as? String {
            if (status == "Non-organic") {
                if let sourceID = data["media_source"],
                   let campaign = data["campaign"] {
                    print("appsflyer This is a Non-Organic install. Media source: \(sourceID)  Campaign: \(campaign)")
                }
            } else {
                print("appsflyer This is an organic install.")
            }
            if let is_first_launch = data["is_first_launch"] as? Bool,
               is_first_launch {
                print("appsflyer First Launch")
            } else {
                print("appsflyer Not First Launch")
            }
        }
    }
    
    func onConversionDataFail(_ error: Error) {
        print("\(error)")
    }
}
