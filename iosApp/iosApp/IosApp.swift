//
//  MusicFeatureIosApp.swift
//  MusicFeatureIos
//
//  Created by vlad on 7/4/21.
//

import SwiftUI
import shared
import Firebase
import Adapty
import Kingfisher
import Amplitude
import FacebookCore
import FacebookAEM


 @main
struct IosApp: App {
    
    @UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
    
    init() {
//        Adapty.logLevel = .verbose
        Adapty.activate("public_live_place_api_key_here_0000000000") { result in
            print("adapty activate result \(result?.localizedDescription)")
        }
        Amplitude.instance().initializeApiKey("amplitude_api_key")
        FirebaseApp.configure()
        URLCache.shared.memoryCapacity = 1
        Dependencies.registerAppDependencies()
    }
    
    
    var body: some Scene {
        WindowGroup{
            RootView {
                MainScreenView()
            }
        }
    }
}
