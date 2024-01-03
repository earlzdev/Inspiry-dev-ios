//
//  UILabelTest.swift
//  iosApp
//
//  Created by rst10h on 25.04.22.
//

import Foundation
import SwiftUI

struct UILabelTest: UIViewRepresentable {
   
    let label = UILabel(frame: .zero)
    
    func makeUIView(context: UIViewRepresentableContext<UILabelTest>) -> UIView {
        
        label.text = "123123123123123"
        label.backgroundColor = .green.withAlphaComponent(0.3)
        return label
    }
    
    func updateUIView(_ uiView: UIView, context:  UIViewRepresentableContext<UILabelTest>) {
        
    }
    

}
