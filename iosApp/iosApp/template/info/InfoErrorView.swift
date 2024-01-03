//
//  InfoErrorView.swift
//  iosApp
//
//  Created by vlad on 5/11/21.
//

import SwiftUI
import shared

struct InfoErrorView: View {
    let infoViewColors: InfoViewColors
    let displayInfoText: Bool
    let reloadTemplate: () -> ()
    var error: KotlinThrowable? = nil
    
    var body: some View {
        VStack {
            
            if displayInfoText {
                Text(MR.strings().error_to_load_template_subtitle.localized())
                    .font(.system(size: 14))
                    .foregroundColor(infoViewColors.text.toSColor())
                    .padding(.horizontal, 20)
                
                Button(action: reloadTemplate, label: {
                    Text(MR.strings().error_to_load_template_button.localized())
                        .padding(.horizontal, 14)
                        .padding(.vertical, 4)
                        .font(.system(size: 17, weight: .medium))
                })
                    
            }
        }.padding(.bottom, 30)
    }
}

struct InfoErrorView_Previews: PreviewProvider {
    static var previews: some View {
        ZStack {
            InfoErrorView(infoViewColors: InfoViewColorsDark(), displayInfoText: true, reloadTemplate: {})
        }.frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
