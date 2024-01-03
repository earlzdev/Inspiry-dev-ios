//
//  MultilineTextEditor.swift
//  iosApp
//
//  Created by rst10h on 25.07.22.
//

import SwiftUI

struct MultilineTextEditor: UIViewRepresentable {
    
    @Binding var text: String
    @Binding var keyboardVisible: Bool
    let font: UIFont = .systemFont(ofSize: 30)
    
    func makeUIView(context: UIViewRepresentableContext<Self>) -> VerticallyCenteredTextView {
        let view = VerticallyCenteredTextView()
        
        view.isScrollEnabled = true
        view.isEditable = true
        view.isUserInteractionEnabled = true
        view.backgroundColor = .clear
        view.textColor = .black
        view.textAlignment = .center
        view.becomeFirstResponder()
        NotificationCenter.default.addObserver(forName: UITextView.textDidChangeNotification,
                                               object: view,
                                               queue: .main) { (notification) in
            if let tv = notification.object as? UITextView {
                text = tv.text
            }
        }
        
        updateUIView(view, context: context)
        
        return view
    }
    
    func updateUIView(_ uiView: VerticallyCenteredTextView, context: UIViewRepresentableContext<Self>) {
        
        let style = NSMutableParagraphStyle()
        style.alignment = .center
        let color: UIColor = keyboardVisible ? .black : .clear
        uiView.tintColor = keyboardVisible ? nil : .clear
        let attributes = [
            NSAttributedString.Key.paragraphStyle: style,
            NSAttributedString.Key.foregroundColor: color,
            NSAttributedString.Key.font: font
        ]
        let text = NSAttributedString(string: text,
                                      attributes: attributes)
        uiView.attributedText = text
        uiView.typingAttributes = attributes
        
        
        
    }
}

class VerticallyCenteredTextView: UITextView {
    
    override var contentSize: CGSize {
        didSet {
            centerTextAlignment()
        }
    }
    
    override var frame: CGRect {
        didSet {
            centerTextAlignment()
        }
    }
    
    private func centerTextAlignment() {
        var topCorrection = (bounds.size.height - contentSize.height * zoomScale) / 2.0
        topCorrection = max(0, topCorrection)
        contentInset = UIEdgeInsets(top: topCorrection, left: 0, bottom: 0, right: 0)
    }
}
