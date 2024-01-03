//
//  VectorView.swift
//  iosApp
//
//  Created by rst10h on 11.04.22.
//

import Foundation
import Lottie
import SwiftUI
import shared

struct VectorLottieViewUI: UIViewRepresentable {
    let inspView: InspVectorView
    let innerView: InnerVectorViewApple?
    
    init(inspView: InspVectorView) {
        self.inspView = inspView
        self.innerView = inspView.innerVectorView as? InnerVectorViewApple
    }
    
    func makeUIView(context: UIViewRepresentableContext<VectorLottieViewUI>) -> UIView {
        guard let innerView = self.innerView else { return UIView() }
        if let svg = innerView.svgView {
            if let helper = inspView.movableTouchHelper as? MovableTouchHelperApple {
                svg.setOnClickListener {
                    inspView.setSelected()
                }
                svg.setOnDragListener(isWrapper: false, action: helper.onDragEvent)
            }
            return svg
        } else {
            if let helper = inspView.movableTouchHelper as? MovableTouchHelperApple {
                innerView.lottieView.setOnClickListener {
                    inspView.setSelected()
                }
                innerView.lottieView.setOnDragListener(isWrapper: false, action: helper.onDragEvent)
            }
            return innerView.lottieView
        }
    }
    
    func updateUIView(_ uiView: UIView, context: UIViewRepresentableContext<VectorLottieViewUI>) {
        
    }
}
