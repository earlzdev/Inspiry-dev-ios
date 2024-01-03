//
//  OffsetableScrollView.swift
//  iosApp
//
//  Created by rst10h on 25.03.22.
//

import SwiftUI
import Combine

struct OffsetableScrollView<Content>: View where Content: View {
    let axis: Axis.Set
    private var onScrollAction: ((CGFloat) -> Void)?
       
    let content: Content
   
    init(_ axis: Axis.Set, @ViewBuilder content: () -> Content) {
        self.axis = axis
        self.content = content()
        
    }
    
    init(@ViewBuilder content: () -> Content) {
        self.init(.vertical, content: content)
    }
    
    var body: some View {
        
        ScrollView(axis, showsIndicators: false) {
            ZStack {
                GeometryReader { geo in
                    Color.clear
                        .preference(
                            key: ScrollOffsetPreferenceKey.self,
                            value: axis == .vertical ? -geo.frame(in: .named("scrollView")).minY : -geo.frame(in: .named("scrollView")).minX
                        )
                }
                self.content
            }
        }
        .coordinateSpace(name: "scrollView")
        .onPreferenceChange(ScrollOffsetPreferenceKey.self) {
            if (onScrollAction != nil) {
                onScrollAction!($0)
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
    typealias Value = CGFloat
    static var defaultValue: CGFloat = .zero
    static func reduce(value: inout Value, nextValue: () -> Value) {
        value += nextValue()
    }
}
