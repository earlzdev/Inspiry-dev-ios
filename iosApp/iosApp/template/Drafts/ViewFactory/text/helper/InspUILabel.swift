//
//  InspUILabel.swift
//  iosApp
//
//  Created by rst10h on 18.08.22.
//

import Foundation
import UIKit

class InspUILabel: InspUIView {
    
    
    var text: String {
        didSet {
            if let attrString = (attributedText as? NSMutableAttributedString) {
            attrString.mutableString.setString(text)
            attributedText = attrString
            }
        }
    }
    
    var attributedText: NSAttributedString?
    
    var numberOfLines: Int = 0
    
    var preferredMaxLayoutWidth: CGFloat = 300
    
    var lineBreakMode: NSLineBreakMode = NSLineBreakMode.byWordWrapping {
        didSet {
            refreshStyle()
        }
    }
    
    var adjustsFontForContentSizeCategory = false
    
    var font: UIFont = UIFont() {
        didSet {
            setNewFont(font)
        }
    }
    
    var textAlignment: NSTextAlignment = .center {
        didSet {
            refreshStyle()
        }
    }
    
    let overlapLayer: CALayer
    
//    override func point(inside point: CGPoint, with event: UIEvent?) -> Bool {
//        return false
//    }
    
    init(_ text: String) {
        self.text = ""
        attributedText = NSAttributedString(string: text)
        overlapLayer = CALayer()
        super.init(frame: .zero)
        self.layer.insertSublayer(overlapLayer, at: 0)
    }
    
//    func initLayers() {
//        let frame = self.frame
//        backgroundLayer.frame = CGRect(x: -10, y: -10, width: frame.size.width + 20, height: frame.size.height + 20)
//        backgroundLayer.borderColor = UIColor.blue.cgColor
//        backgroundLayer.borderWidth = 1
//        backgroundLayer.backgroundColor = UIColor.black.withAlphaComponent(0.3).cgColor
//    }
    

    
    var textColor: UIColor = .darkText {
        didSet {
            DispatchQueue.main.async {
                self.setNeedsDisplay()
            }

        }
    }
    
    func updateBackgroundColor() {
        
    }
  
    
    override func draw(_ rect: CGRect) {
        drawText(in: rect)
        }
        
    func drawText(in rect: CGRect) { }
    
    private func setNewFont(_ font: UIFont) {
        
        guard let attributedText = attributedText else {
            return
        }

        let string = NSMutableAttributedString(attributedString: attributedText)
        string.addAttribute(NSAttributedString.Key.font, value: font, range: NSRange(location: 0, length: string.length))
        self.attributedText = string
    }
    
    private func refreshStyle() {
        guard let attributedText = attributedText else {
            return
        }

        let string = NSMutableAttributedString(attributedString: attributedText)
        let style = getParagraphStyle()
        style.lineBreakMode = lineBreakMode
        style.alignment = textAlignment
        string.addAttribute( .paragraphStyle, value: style, range: NSRange(location: 0, length: string.length))
        self.attributedText = string
    }
    
    func getParagraphStyle() -> NSMutableParagraphStyle {

        guard let string = attributedText else { return NSMutableParagraphStyle()}
        
        var result: NSMutableParagraphStyle? = nil
        let style = string.enumerateAttribute(.paragraphStyle, in: NSRange(location: 0, length: string.length)) { value, range, stop in
            if let style = value as? NSParagraphStyle {
                result = (style as? NSMutableParagraphStyle)
            }
        }
        
        return result ?? NSMutableParagraphStyle()
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    override func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool {
        return false
    }
}
