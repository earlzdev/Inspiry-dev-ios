//
//  LottieView.swift
//  iosApp
//
//  Created by vlad on 14/1/22.
//

import Foundation
import Lottie
import SwiftUI
import shared

struct LottieView: UIViewRepresentable {
    let name: String
    let isPlaying: Bool
    let bundle: Bundle
    let loopMode: LottieLoopMode
    let contentMode: UIView.ContentMode
    
    
    init(name: String, isPlaying: Bool, bundle: Bundle = Bundle.main, loopMode: LottieLoopMode = .playOnce, contentMode: UIView.ContentMode = .scaleAspectFit) {
        self.name = name
        self.isPlaying = isPlaying
        self.bundle = bundle
        self.loopMode = loopMode
        self.contentMode = contentMode
    }
    
    private var animationView = LottieAnimationView()
    
    func makeUIView(context: UIViewRepresentableContext<LottieView>) -> UIView {
        let view = UIView(frame: .zero)
        
        animationView.animation = LottieAnimation.named(name, bundle: bundle)
        
        animationView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(animationView)
        
        NSLayoutConstraint.activate([
            animationView.heightAnchor.constraint(equalTo: view.heightAnchor),
            animationView.widthAnchor.constraint(equalTo: view.widthAnchor)
        ])
        
        updateUIView(animationView, context: context)
        
        return view
    }
    
    func updateUIView(_ uiView: UIView, context: UIViewRepresentableContext<LottieView>) {
        animationView.loopMode = loopMode
        animationView.contentMode = contentMode
        if isPlaying {
            animationView.play()
        } else {
            animationView.pause()
        }
    }
}
