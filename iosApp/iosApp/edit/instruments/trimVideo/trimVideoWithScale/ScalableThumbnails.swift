//
//  ScalableThumbnails.swift
//  iosApp
//
//  Created by rst10h on 17.10.22.
//

import SwiftUI
import shared

struct ThumbnailsViewScalable: UIViewRepresentable {
    @StateObject
    var trimModel: ScalableTrimSliderModel
    

    func makeUIView(context: UIViewRepresentableContext<ThumbnailsViewScalable>) -> VideoThumbnailsUI {
        let view = VideoThumbnailsUI()
        return view
    }
    
    func updateUIView(_ uiView: VideoThumbnailsUI, context:  UIViewRepresentableContext<ThumbnailsViewScalable>) {
        if let url = trimModel.url {
            uiView.updateThumbnails(videoURL: url)
        }

    }
    
}
