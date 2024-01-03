//
//  SwiftUIExtensions.swift
//  iosApp
//
//  Created by vlad on 18/1/22.
//

import Foundation
import SwiftUI
import shared

extension View {
    
    func alignView(templateView: InspTemplateViewApple, inspView: InspView<AnyObject>) -> some View {
        self.modifier(AlignByModifier(templateView: templateView, inspView: inspView))
    }
    
    func conditionalModifier<T>(condition: Bool, modifier: T) -> some View where T: ViewModifier {
        // group here serves 2 purposes:
        // 1. apply the modifier to all children ?
        // 2. to satisfy opaque type requirement
        Group {
            if condition {
                self.modifier(modifier)
            }
        }
    }
    
    func conditionalModifiers<T1, T2>(condition: Bool, modifierIfTrue: T1, modifierIfFalse: T2) -> some View where T1: ViewModifier, T2: ViewModifier {
        
        Group {
            if condition {
                self.modifier(modifierIfTrue)
            } else {
                self.modifier(modifierIfFalse)
            }
        }
    }
    
    /// Applies the given transform if the given condition evaluates to `true`.
    /// - Parameters:
    ///   - condition: The condition to evaluate.
    ///   - transform: The transform to apply to the source `View`.
    /// - Returns: Either the original `View` or the modified `View` if the condition is `true`.
    @ViewBuilder func `if`<Content: View>(_ condition: @autoclosure () -> Bool, transform: (Self) -> Content) -> some View {
        if condition() {
            transform(self)
        } else {
            self
        }
    }
    /// Applies the given transform if the given condition evaluates to `true`.
    /// - Parameters:
    ///   - condition: The condition to evaluate.
    ///   - transform: The transform to apply to the source `View`.
    ///   - elseTransform: The transform to apply to the source `View` when condition `false`.
    /// - Returns: modified view
    @ViewBuilder func `ifelse`<Content: View>(_ condition: @autoclosure () -> Bool, transform: (Self) -> Content, elseTransform: (Self) -> Content) -> some View {
        if condition() {
            transform(self)
        } else {
            elseTransform(self)
        }
    }
}

struct AlignByModifier: ViewModifier {
    let templateView: InspTemplateViewApple
    let inspView: InspView<AnyObject>
    
    func body(content: Content) -> some View {
        let dy = (templateView.templateHeight - inspView.viewHeight.cg)/2.cg
        let dx = (templateView.templateWidth -  inspView.viewWidth.cg)/2.cg
        let alignBy = inspView.media.layoutPosition?.alignBy ?? .topStart
        switch alignBy {
        case .topStart: content.offset(x: -dx, y: -dy)
        case .topCenter: content.offset(x: 0, y: -dy)
        case .topEnd: content.offset(x: dx, y: -dy)
        case .centerStart: content.offset(x: -dx, y: 0)
        case .centerEnd: content.offset(x: dx, y: 0)
        case .bottomStart: content.offset(x: -dx, y: dy)
        case .bottomCenter: content.offset(x: 0, y: dy)
        case .bottomEnd: content.offset(x: dx, y: dy)
        default:
            content
        }
    }
}
