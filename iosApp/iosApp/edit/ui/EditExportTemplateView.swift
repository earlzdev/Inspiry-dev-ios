//
//  EditExportTemplateView.swift
//  iosApp
//
//  Created by vlad on 8/11/21.
//

import SwiftUI
import shared

struct EditExportTemplateView: View {
    let templateModel: InspTemplateViewApple
    
    var body: some View {
        ZStack {
                TemplateUIView(templateModel: templateModel, autoplay: false, playSound: true)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .aspectRatio(9.0 / 16.0, contentMode: ContentMode.fit)
        .background(Color.white)
        .cornerRadius(16)
        .shadow(color: Color.init(red: 0, green: 0, blue: 0, opacity: 0.20), radius: 4, x: 0, y: 0)
    }
}

//struct EditExportTemplateView_Previews: PreviewProvider {
//    static var previews: some View {
//        EditExportTemplateView(template: getTestTemplate())
//    }
//}
