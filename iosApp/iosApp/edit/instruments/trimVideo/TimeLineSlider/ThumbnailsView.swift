//
//  ThumbnailsView.swift
//  iosApp
//
//  Created by rst10h on 27.07.22.
//

import SwiftUI
import shared

struct ThumbnailsViewRelresentable: UIViewRepresentable {
    @StateObject
    var trimModel: TrimVideoModel

    func makeUIView(context: UIViewRepresentableContext<ThumbnailsViewRelresentable>) -> VideoThumbnailsUI {
        let view = VideoThumbnailsUI()
        return view
    }
    
    func updateUIView(_ uiView: VideoThumbnailsUI, context:  UIViewRepresentableContext<ThumbnailsViewRelresentable>) {
        if let url = trimModel.url {
            uiView.updateThumbnails(videoURL: url)
        }

    }
    
}
