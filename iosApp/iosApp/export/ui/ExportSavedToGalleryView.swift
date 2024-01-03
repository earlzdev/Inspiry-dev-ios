//
//  ExportSavedToGalleryView.swift
//  iosApp
//
//  Created by vlad on 14/1/22.
//

import SwiftUI
import shared

struct ExportSavedToGalleryView: View {
    let colors: EditColors
    
    @State
    private var textVisible: Bool = false
    
    var body: some View {
        Button(action: {
            // TODO: open video in external player ?
            UIApplication.shared.open(URL(string:"photos-redirect://")!)
        }) {
            HStack(spacing: 12) {
                
                let mrAnimation = MR.assetsJson().export_save_to_gallery
                
                LottieView(name: mrAnimation.fileName, isPlaying: true, bundle: mrAnimation.bundle).frame(width: 28, height: 28)
                
                Text(MR.strings().share_save_to_gallery.localized())
                    .foregroundColor(colors.exportSaveToGalleryText.toSColor())
                    .font(.system(size: 16, weight: .bold))
                    .lineLimit(1)
                    .opacity(textVisible ? 1 : 0)
                    .offset(y: textVisible ? 0 : 14)
                    .animation(.easeOut, value: textVisible)
                
            }.onAppear {
                textVisible = true
            }
        }
    }
}

struct ExportSavedToGalleryView_Previews: PreviewProvider {
    static var previews: some View {
        ExportSavedToGalleryView(colors: EditColorsLight())
    }
}
