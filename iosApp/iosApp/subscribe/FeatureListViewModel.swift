//
//  InfiniteScrollingModel.swift
//  iosApp
//
//  Created by rst10h on 5.12.22.
//

import Foundation
import SwiftUI
import shared

class FeatureListViewModel: ObservableObject {
    @Published
    var items = [0,1,2,3,4,5]

    @Published var offset: CGFloat = 0
    
    
    var featuresList = [
        SubscribeFeature(text: MR.strings().subscribe_feature_all, image: UIImage(named: "feature_all_templates")!),
        SubscribeFeature(text: MR.strings().subscribe_feature_fonts, image: UIImage(named: "feature_fonts")!),
        SubscribeFeature(text: MR.strings().subscribe_feature_watermark, image: UIImage(named: "feature_watermark")!),
        SubscribeFeature(text: MR.strings().subscribe_feature_text_animations, image: UIImage(named: "feature_text_animations")!),
        SubscribeFeature(text: MR.strings().subscribe_feature_format, image: UIImage(named: "feature_formats")!),
        SubscribeFeature(text: MR.strings().remove_bg_promo_title, image: UIImage(named: "feature_remove_bg")!),
    ]
    
    var timer: Timer? = nil
    
    func createTimer() {
      // 1
      if timer == nil {
        // 2
          timer = Timer.scheduledTimer(timeInterval: 1.0/60.0,
                                     target: self,
                                     selector: #selector(updateTimer),
                                     userInfo: nil,
                                     repeats: true)
      }
    }
    
    var scrollInProgress = false
    
    var autoScrollDelta: CGFloat = -0.5
       
    @objc func updateTimer() {
        if (autoScrollDelta > 0.5) {
            autoScrollDelta *= 0.9
            if (autoScrollDelta < 0.5) {
                autoScrollDelta = 0.5
            }
        }
        if (autoScrollDelta < -0.5) {
            autoScrollDelta *= 0.9
            if (autoScrollDelta > -0.5) {
                autoScrollDelta = -0.5
            }
        }
        if !scrollInProgress {
            updateOffset(delta: autoScrollDelta)
        }
    }
    
    @objc func updateOffset(delta: CGFloat) {
        var nextOffset = offset + delta
        if (nextOffset > itemWidth) {
            nextOffset -= itemWidth
            items.move(fromOffsets: [5], toOffset: 0)
            print("moveitems \(items.endIndex) \(items.startIndex)")
        }
        if (nextOffset < -itemWidth) {
            nextOffset += itemWidth
            items.move(fromOffsets: [0], toOffset: 6)
        }
        offset = nextOffset
    }
    
    var itemWidth: CGFloat = 0
    var screenWidth: CGFloat = 0
    
    private var lastTranslation: CGFloat = 0
    private var lastDx: CGFloat = 0
    
    func startScrolling(itemWidth: CGFloat, frameWidth: CGFloat) {
        self.itemWidth = itemWidth
        self.screenWidth = frameWidth
        createTimer()
    }
    
    func onScrollEnded() {
        lastTranslation = 0
        scrollInProgress = false
        autoScrollDelta = lastDx
    }
    
    func onUserScroll(delta: CGFloat) {
        scrollInProgress = true
        let dx = lastTranslation - delta
        updateOffset(delta: -dx)
        lastTranslation = delta
        lastDx = -dx
    }
    
    
    
    func getOffsetForItem(item: Int) -> CGFloat {
        let offset = item.cg * itemWidth - itemWidth + offset
        return offset
    }
    func onAppear(page: Int){
            items.append(page + 1)
    }
}

struct SubscribeFeature {
    var text: StringResource
    var image: UIImage
}
