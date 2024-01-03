//
//  AlertMediaView.swift
//  iosApp
//
//  Created by rst10h on 6.01.23.
//

import SwiftUI
import shared

struct AlertData {
       
    var title: String
    var message: String
    var acceptButton: String
    var cancelButton: String
    var acceptAction: () -> Void
    
    static func getMediaAlert() -> AlertData {
        return AlertData(title: MR.strings().media_library_request_title.localized(),
                         message: MR.strings().media_library_request_description.localized(),
                         acceptButton: MR.strings().settings.localized(),
                         cancelButton: MR.strings().cancel.localized(),
                         acceptAction: {  UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!) })
    }
}

extension Alert {
    init(data: AlertData) {
        self.init(title: Text(data.title) ,message: Text(data.message), primaryButton: .default(Text(data.acceptButton)) { data.acceptAction() }, secondaryButton: .cancel(Text(data.cancelButton)))
    }
}
