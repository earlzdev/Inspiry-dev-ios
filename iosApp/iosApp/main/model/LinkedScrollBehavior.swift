//
//  BannerBehavior.swift
//  iosApp
//
//  Created by rst10h on 28.12.21.
//

import SwiftUI

class LinkedScrollBehavior: ObservableObject {
    let maxOffset: CGFloat
    let minOffset: CGFloat
    
    private var parentScrollPosition: CGFloat = 0
    
    @Published
    var currentOffset: CGFloat = 0
    
    init (minOffset: CGFloat, maxOffset: CGFloat) {
        self.maxOffset = maxOffset
        self.minOffset = minOffset
    }
    
    func parentScrollChanged(newScrollPosition: CGFloat) {
        
        let scroll = -newScrollPosition - parentScrollPosition
        
        parentScrollPosition = -newScrollPosition
        
        guard (parentScrollPosition <= 0) else { return } //scrolling offset is negative by default
        //if (abs(scroll) > abs(maxOffset - minOffset)) { return }
        
        switch (scroll + currentOffset) {
        case let c where c < minOffset: currentOffset = minOffset
        case let c where c > maxOffset: currentOffset = maxOffset
        default: currentOffset += scroll
        }
    }
}
