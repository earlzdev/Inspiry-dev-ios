//
//  ExternalLinks.swift
//  iosApp
//
//  Created by rst10h on 14.01.22.
//

import SwiftUI
import shared

class ExternalLinks {
    static func openInstagram() {
        
        let appURL = URL(string:  InstagramSubscribeHolderKt.INSTAGRAM_PAGE_LINK)
        
        if (appURL != nil && UIApplication.shared.canOpenURL(appURL!)) {
            UIApplication.shared.open(appURL!, options: [:], completionHandler: nil)
        
        } else {
            let webURL = URL(string:  InstagramSubscribeHolderKt.INSTAGRAM_APP_LINK)
            if (webURL != nil) {
                UIApplication.shared.open(webURL!, options: [:], completionHandler: nil)
            }
            
        }
    }
}
