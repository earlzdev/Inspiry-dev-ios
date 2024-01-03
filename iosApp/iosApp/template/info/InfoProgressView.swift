//
//  InfoProgressView.swift
//  iosApp
//
//  Created by vlad on 5/11/21.
//

import SwiftUI
import shared

struct InfoProgressView: View {
    let infoViewColors: InfoViewColors
    let displayInfoText: Bool
    let stateValue: KotlinFloat?
    let cancelLoadImages: () -> ()
    
    var body: some View {
        
        VStack {
            ProgressView().progressViewStyle(CircularProgressViewStyle(tint: infoViewColors.progressIndicator.toSColor()))
            
            if displayInfoText {
                Text(MR.strings().template_is_loading.localized())
                    .font(.system(size: 14))
                    .foregroundColor(infoViewColors.text.toSColor())
                    .padding(.top, 6)
                    .padding(.horizontal, 20)
                
                if stateValue != nil && Float(truncating: stateValue!) == InfoViewModelImpl.companion.VALUE_IMAGES {
                    
                    Button(action: cancelLoadImages, label: {
                        Text(MR.strings().cancel.localized())
                            .padding(.horizontal, 14)
                            .padding(.vertical, 4)
                            .font(.system(size: 17, weight: .medium))
                    })
                }
            }
        }.padding(.bottom, 30)
    }
}

struct InfoProgressView_Previews: PreviewProvider {
    static var previews: some View {
        ZStack {
            InfoProgressView(infoViewColors: InfoViewColorsDark(), displayInfoText: true, stateValue: KotlinFloat(value: InfoViewModelImpl.companion.VALUE_IMAGES), cancelLoadImages: {})
        }.frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
