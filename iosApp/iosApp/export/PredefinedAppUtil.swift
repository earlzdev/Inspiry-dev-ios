//
// Created by vlad on 10/12/21.
//

import Foundation
import shared
import UIKit


func isAppInstalled(_ appName:String) -> Bool {

    let appScheme = "\(appName)://app"
    let appUrl = URL(string: appScheme)

    // TODO: check on the real device and remove debug.
    if Dependencies.isDebug() || UIApplication.shared.canOpenURL(appUrl! as URL){
        return true
    } else {
        return false
    }
}

func getPredefinedExportApps() -> [PredefinedExportApp] {
    return PredefinedExportAppKt.getDefaultPredefinedExportApps().filter { app in isAppInstalled(app.whereToExport.whereApp) }
}
