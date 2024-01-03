//
//  InspTrimVideoView.swift
//  iosApp
//
//  Created by rst10h on 19.10.22.
//

import SwiftUI
import AVFoundation
import shared

struct InspTrimVideoView: UIViewRepresentable {
    
    @ObservedObject
    var model: ScalableTrimSliderModel
    var container: UIView = UIView()
    
    func makeUIView(context: UIViewRepresentableContext<InspTrimVideoView>) -> VideoTimelineView {
        model.isUserAction = false
        let timelineView = VideoTimelineView()
        timelineView.frame = CGRect(x: 0, y: 0, width: UIScreen.screenWidth, height: 50)
        timelineView.timelineView.videoScale = model.currentScale
        timelineView.new(url: model.url)
        timelineView.timelineView.onOffsetUpdated = model.onOffsetUpdated
        model.autoscroll = { value, _ in
            timelineView.timelineView.scroller.setScrollValue(value)
            
        }
        model.scrollTo = timelineView.timelineView.scroller.setScroll(point:)
        let start = model.startOffset
        print("start offset while init = \(start)")
        timelineView.timelineView.scroller.contentOffset.x = start
        timelineView.timelineView.scroller.frameImagesView.requestVisible(depth:0, wide:0, direction:0)
        timelineView.timelineView.scroller.frameImagesView.displayFrames()
        model.onOffsetUpdated(newValue: 0, rawNew: start, rawOld: 0)
        return timelineView
    }
    
    func updateUIView(_ uiView: VideoTimelineView, context:  UIViewRepresentableContext<InspTrimVideoView>) {
        if (model.autoscroll == nil) {
            uiView.timelineView.onOffsetUpdated = model.onOffsetUpdated
            model.autoscroll = { value, maxOver in
                uiView.timelineView.scroller.setScrollValue(value, maxOver: maxOver)
            }
            model.scrollTo = uiView.timelineView.scroller.setScroll(point:)
        }
        let isNewUrl = model.url != uiView.url
        print("is new video url.. \(isNewUrl) \(model.url) | \(uiView.url)")
        if (uiView.timelineView.videoScale != model.currentScale || isNewUrl) {
            print("is new video url")
            model.isUserAction = false
            uiView.timelineView.scroller.stop()
            let old_scale = uiView.timelineView.videoScale
            let new_scale = model.currentScale

            var start_offset = (uiView.timelineView.scroller.contentOffset.x + model.cachedLeadingPosition) * new_scale / old_scale
            if isNewUrl {
                start_offset = model.startOffset
            }
            uiView.timelineView.scroller.contentOffset.x = 0.cg
            uiView.timelineView.videoScale = model.currentScale
            uiView.new(url: model.url)
            uiView.timelineView.scroller.contentOffset.x = start_offset
            uiView.timelineView.scroller.frameImagesView.requestVisible(depth:0, wide:0, direction:0)
            uiView.timelineView.scroller.frameImagesView.displayFrames()
            model.onOffsetUpdated(newValue: 0, rawNew: start_offset, rawOld: 0)
        }
    }
    
    
    
}


struct InspTrimVideoView_Previews: PreviewProvider {
    
    static var previews: some View {
        InspTrimVideoView(model: ScalableTrimSliderModel(url: Bundle.main.url(forResource: "countdown", withExtension: "mp4")!))
            .frame(maxWidth: .infinity)
            .background(0xff202020.ARGB)
    }
}
