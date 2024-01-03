//
//  TextView.swift
//  iosApp
//
//  Created by rst10h on 19.04.22.
//

import SwiftUI
import Kingfisher
import shared

struct TextViewUI: UIViewRepresentable {
    let inspView: InspTextView
    
    init(inspView: InspTextView) {
        self.inspView = inspView
        
    }

    func makeUIView(context: UIViewRepresentableContext<TextViewUI>) -> UIView {
        guard
            let tv = (inspView.textView as? InnerTextHolderApple)?.textView
        else { return UIView() }
        
        updateUIView(tv, context: context)
        if let helper = inspView.movableTouchHelper as? MovableTouchHelperApple {
            tv.setOnDragListener(isWrapper: false, action: helper.onDragEvent)
        }
        
        
        return tv
    }
    
    func updateUIView(_ uiView: UIView, context: UIViewRepresentableContext<TextViewUI>) {

    }
}

