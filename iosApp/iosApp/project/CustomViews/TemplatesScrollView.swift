//
//  TemplatesScrollView.swift
//  iosApp
//
//  Created by rst10h on 4.01.22.
//

import SwiftUI

struct TemplatesScrollView<Content>: View where Content: View {
    private var onScrollAction: ((CGFloat) -> Void)?
    let content: Content
    
    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }
    
    var body: some View {
        
        ScrollView(.vertical, showsIndicators: false) {
            GeometryReader { geo in
                let y = geo.frame(in: .named("scrollView")).origin.y
                Color.clear.preference(
                    key: ScrollOffsetPreferenceKey.self,
                    value: y
                )
            }
            
            VStack {
                self.content
                
            }
            
        }
        .coordinateSpace(name: "scrollView")
        .onPreferenceChange(ScrollOffsetPreferenceKey.self) { of in
            if (onScrollAction != nil && of < 0) {
                onScrollAction!(of)
            }
        }
        
    }
    
    
    func onScroll(perform action: @escaping (CGFloat) -> Void) -> Self {
        var copy = self
        copy.onScrollAction = action
        return copy
    }
}

private struct ScrollOffsetPreferenceKey: PreferenceKey {
    static var defaultValue: CGFloat = .zero
    static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) {}
}
