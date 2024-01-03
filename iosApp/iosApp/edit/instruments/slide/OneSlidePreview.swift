//
//  OneSlidePreview.swift
//  iosApp
//
//  Created by rst10h on 5.09.22.
//

import SwiftUI
import AVFoundation
import CoreMedia
import Kingfisher

struct OneSlidePreview: View {
    
    @StateObject
    var model: SlidePreviewModel
    
    var body: some View {
        
        if let image = model.preview {
            Image(uiImage: image)
                .resizable()
                .aspectRatio(contentMode: .fill)
                .frame(width: 34.cg, height: 34.cg)
                .clipShape(RoundedRectangle(cornerRadius: 6))
        } else {
            ProgressView()
                .frame(width: 34.cg, height: 34.cg)
                .clipShape(RoundedRectangle(cornerRadius: 6))
        }

    }
}
