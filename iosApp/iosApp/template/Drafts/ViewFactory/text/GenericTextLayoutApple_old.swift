//
//  InspTextViewUI.swift
//  iosApp
//
//  Created by rst10h on 19.04.22.
//

import Foundation
import SwiftUI
import shared
import UIKit

class GenericTextLayoutApple_Old: UILabel, InnerGenericText {
    
    var fullText: String {
        get {
            return text ?? ""
        }
        set {
            genericTextHelper?.recompute(text: newValue)
            super.text = newValue
        }
    }
    
    
    let media: MediaText
    
    var genericTextHelper: GenericTextHelper<AnyObject>? = nil
    var needsRecompute: Bool = true
    var radius: Float = 0
    
    var textBounds = CGRect()
    
    var currentFrame: Int32 = 0 {
        didSet {
            DispatchQueue.main.async {
                self.setNeedsDisplay()
            }
            
        }
    }
    
    var circularTextSize: Float = 0
    var templateWidth: Int32 = 0
    var templateHeight: Int32 = 0
    var circularGravity: TextAlign
    var circularRadius: Float = 0
    
    lazy var tempTextAnimParam = DrawTextAnimParamApple()
    
    func refresh() {
        
    }
    
    func getLineForOffset(offset: Int32) -> Int32 {
        let line = textLines.lineNumberForCharIndex(charIndex: offset.int).int32
        print ("line number \(line) for offset \(offset)")
        return line
    }
    
    func drawTextBackgroundsSingle(animParam: TextAnimationParams, canvas: Any?, time: Double, out: Bool, shadowMode: Bool) {
        
    }
    
    var onFrameUpdated: ((_ bounds: CGRect) -> Void)?
    
    override func layoutSubviews() {
        super.layoutSubviews()
        onFrameUpdated?(self.bounds)
        recompute()
        //frame size was changed
    }
    
    private var textLines: TextLines = TextLines()
    
    func updateLinesCountForText() {
        guard let attributedText = attributedText else { return }
        let textStorage = NSTextStorage(attributedString: attributedText)
        let layoutManager = NSLayoutManager()
        textStorage.addLayoutManager(layoutManager)
        
        var numberOfLines: UInt = 0
        var index: UInt = 0
        let numberOfGlyphs = UInt(layoutManager.numberOfGlyphs ?? 0)
        
        let isInWrapContent = media.layoutPosition.isInWrapContentMode()
        
        let textContainer = NSTextContainer(size: CGSize(width: isInWrapContent ? textBounds.width : frame.size.width, height: CGFloat.greatestFiniteMagnitude))
        textContainer.lineFragmentPadding = 0.0
        textContainer.lineBreakMode = .byWordWrapping
        
        layoutManager.addTextContainer(textContainer)
        
        var linesIndexes: [Int] = []
        
        var lineRange: NSRange = NSRange()
        while index < numberOfGlyphs {
            layoutManager.lineFragmentRect(
                forGlyphAt: Int(index),
                effectiveRange: &lineRange)
            index = UInt(NSMaxRange(lineRange))
            linesIndexes.append(Int(index))
            numberOfLines += 1
        }
        textLines.linesCharIndex = linesIndexes
    }
    
    func boundingRectForCharacterRange(_ range: NSRange) -> CGRect? {
        
        updateLinesCountForText()
        
        guard let attributedText = attributedText else { return nil }
        
        let textStorage = NSTextStorage(attributedString: attributedText)
        let layoutManager = NSLayoutManager()
        
        textStorage.addLayoutManager(layoutManager)
        
        let isInWrapContent = media.layoutPosition.isInWrapContentMode()
        
        let textContainer = NSTextContainer(size: CGSize(width: isInWrapContent ? textBounds.width : frame.size.width, height: CGFloat.greatestFiniteMagnitude))
        textContainer.lineFragmentPadding = 0.0
        textContainer.lineBreakMode = .byWordWrapping
        
        layoutManager.addTextContainer(textContainer)
        
        var glyphRange = NSRange()
        
        // Convert the range for glyphs.
        layoutManager.characterRange(forGlyphRange: range, actualGlyphRange: &glyphRange)
        
        return layoutManager.boundingRect(forGlyphRange: glyphRange, in: textContainer)
    }
    
    func setStartTimeSource(source: @escaping () -> KotlinInt) {
        genericTextHelper?.getStartTime = source
    }
    
    func setDurationSource(source: @escaping () -> KotlinInt) {
        genericTextHelper?.getDuration = source
    }
    
    func drawPart(canvas: Any?, time: Double, partInfo: PartInfo, textAnimationParams: TextAnimationParams, animatorOut: Bool, partIndex: Int32, partsCount: Int32, lineNumber: Int32) {
        
        
        if (time >= partInfo.startTime || animatorOut) {
            let index = partInfo.index
            //todo isRtl support??
            
            var startIndex = fullText.index(fullText.startIndex, offsetBy: index.int)
            var endIndex = fullText.index(fullText.startIndex, offsetBy: index.int + partInfo.length.int - 1)
            if (fullText[endIndex] == "\n") {
                endIndex = fullText.index(fullText.startIndex, offsetBy: index.int + partInfo.length.int - 2)
            }
            if (startIndex > endIndex) { return }
            let str = fullText[startIndex...endIndex] as NSString
            var r = boundingRectForCharacterRange(NSRange(startIndex...endIndex, in: fullText))!// ?? CGRect()
            r.origin = CGPoint(x: r.minX, y: 0)
            
            tempTextAnimParam.initializeAndNullify(
                charSequence: fullText,
                isRtl: false,
                startTextToRender: index,
                lengthTextToRender: partInfo.length,
                strokeWidth: 0,
                lineBaseline: 0,
                paddingTop: 0, lineTop: 0)
            
            tempTextAnimParam.height = r.height.toInt32
            tempTextAnimParam.left = r.minX.float
            tempTextAnimParam.top = r.minY.float
            tempTextAnimParam.width = r.width.toInt32
            
            genericTextHelper?.applyAnimation(
                textAnimationParams: textAnimationParams,
                time: time,
                partStartTime: partInfo.startTime,
                backgroundAnimParam: tempTextAnimParam,
                partIndex: partIndex,
                partsCount: partsCount,
                view: self,
                shadowMode: false,
                out: animatorOut)
            r = r.offsetBy(dx: tempTextAnimParam.translateX.cg, dy: tempTextAnimParam.translateY.cg + 0)
            
            var y_correction = 0.cg
                y_correction = font.lineHeight * lineNumber.int.cg * lineSpacing
                r.size = CGSize(width: r.width, height: font.lineHeight)
                r = r.offsetBy(dx: 0, dy: y_correction)
            
            var attributes = attributedText!.attributes(at: 0, effectiveRange: nil)
            //attributes.append([NSAttributedString.Key.foregroundColor: self.textColor.withAlphaComponent(tempTextAnimParam.alpha.cg)])
            
            //            let ln = (tempTextAnimParam.top.cg / r.height).rounded()
            //            print("line number \(ln)")
            //            if (ln > 0) {
            //                var y_correction = 0.cg
            //                if (r.height > font.lineHeight) {
            //                    y_correction = (r.height - font.lineHeight) * ln
            //                    r.size = CGSize(width: r.width, height: font.lineHeight)
            //                }
            //                let lineSpacingOffset = (1 - media.lineSpacing.cg) * ln * font.lineHeight + y_correction
            //                r = r.offsetBy(dx: 0, dy: -lineSpacingOffset)
            //
            //            }
            
            let context = UIGraphicsGetCurrentContext()!
            context.saveGState()
            
            let scaleDx: CGFloat = tempTextAnimParam.scaleX.cg * r.width - r.width
            let scaleDy: CGFloat = tempTextAnimParam.scaleY.cg * r.height - r.height
            
            context.translateBy(x: -scaleDx/2, y: -scaleDy/2)
            context.scaleBy(x: tempTextAnimParam.scaleX.cg, y: tempTextAnimParam.scaleY.cg)
            
            if let shadowColor = media.textShadowColor {
                let shadowC: UIColor = UIColor(cgColor: shadowColor.intValue.ARGB.cgColor!)
                attributes[NSAttributedString.Key.foregroundColor] = shadowC
                let shadowRect = r.offsetBy(dx: media.shadowOffsetX?.floatValue.cg ?? 0.cg, dy: media.shadowOffsetY?.floatValue.cg ?? 0.cg)
                str.draw(in: r, withAttributes: attributes)
            }
            
            if let shadowColors = media.shadowColors {
                let shadowC: UIColor = UIColor(cgColor: (shadowColors[0] as! KotlinInt).intValue.ARGB.cgColor!)
                attributes[NSAttributedString.Key.foregroundColor] = shadowC
                let shadowRect = r.offsetBy(dx: (media.shadowOffsetX?.floatValue.cg ?? 0.cg)  * font.lineHeight, dy: (media.shadowOffsetY?.floatValue.cg ?? 0.cg)  * font.lineHeight)
                str.draw(in: shadowRect, withAttributes: attributes)
            }
            
            attributes[NSAttributedString.Key.foregroundColor] = self.textColor.withAlphaComponent(tempTextAnimParam.alpha.cg)
            str.draw(in: r, withAttributes: attributes)
            //UIRectFrame(r)
            context.restoreGState()
            
            
        }
    }
    
    private func calcTopOffsetForVerticalGravity() -> Int {
        return 0
    }
    
    
    func recompute() {
        updateLinesCountForText()
        print ("line numbers: \(textLines.linesCharIndex)")
        genericTextHelper!.recompute(text: text ?? "")
    }
    
    func draw(animParam: TextAnimationParams, canvas: Any?, time: Double, out: Bool) {
        genericTextHelper?.drawTextBackgrounds(animParam: animParam, canvas: nil, time: time, out: out)
        genericTextHelper?.drawParts(animParam: animParam, canvas: nil, time: time, out: out)
    }
    
    
    override func drawText(in rect: CGRect) {
        genericTextHelper?.onDrawText(canvas: nil, currentFrame: currentFrame)
    }
   
    required init(media: MediaText) {
        self.media = media
        self.circularGravity = media.innerGravity
        self.needsRecompute = false
        super.init(frame: .zero)
        
        self.genericTextHelper = GenericTextHelper(media: media, layout: self)
        super.text = media.text
        super.numberOfLines = 0
        super.lineBreakMode = .byWordWrapping
        super.adjustsFontForContentSizeCategory = false
        super.backgroundColor = UIColor.black.withAlphaComponent(0.4)
        
        //super.backgroundColor = .black.withAlphaComponent(0.3)
        
        /**
         **                 true text shadow
         */
        
        //        super.layer.masksToBounds = false
        //        super.layer.cornerRadius = 8;
        //        super.layer.shadowOffset = CGSize(width: 10, height: 10);
        //        super.layer.shadowRadius = 5;
        //        super.layer.shadowOpacity = 0.5;
        //        super.layer.shadowColor = ..
        
        
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func getTextFrame() -> CGSize {
        if let font = font {
            let attr = [NSAttributedString.Key.font: font]
            return (text! as NSString).size(withAttributes: attr)
        }
        return .zero
    }
    
    func getTextFrameWIthSize(maxWidth: CGFloat) -> CGSize {
        if let font = font {
            return text?.labelSize(font: font, size: CGSize(width: maxWidth, height: CGFloat.greatestFiniteMagnitude)) ?? .zero
        }
        return .zero
    }
    
    var lineSpacing = 1.cg
    
    var letterSpacing = 1.cg
    
    func setLinespacing(newValue: Float) {
        self.lineSpacing = newValue.cg
    }
    
    func setLetterSpacing(newValue: Float) {
        guard let text = text, !text.isEmpty else { return }
        guard let attributedText = attributedText else { return }
        let string = NSMutableAttributedString(attributedString: attributedText)
        let kernValue = font.pointSize * newValue.cg
        string.addAttribute(NSAttributedString.Key.kern, value: kernValue, range: NSRange(location: 0, length: string.length))
        self.attributedText = string
      }
    
    
    func setTextSize(newSize: CGFloat) {
        setFont(font: font.withSize(newSize))
    }
    
    func setFont(font: UIFont) {
        self.font = font
        updateLinesCountForText()
        
    }
    
    func setAlignment(_ align: TextAlign) {
        textAlignment = align.toAlignment()
    }
    var textRange: NSRange {
        get {
            return NSMakeRange(0, fullText.count)
        }
    }
    
    func _getParagraphStyle() -> NSMutableParagraphStyle? {
        guard var textAttributes = self.attributedText else { return nil}
        var result: NSMutableParagraphStyle? = nil
        let style = textAttributes.enumerateAttribute(.paragraphStyle, in: textRange) { value, range, stop in
            if let style = value as? NSParagraphStyle {
                if (result == nil) { result = style.mutableCopy() as? NSMutableParagraphStyle }
            }
        }
        return result
    }
    
    func labelSize(withConstrainedWidth width: CGFloat, font: UIFont) -> CGSize {
        let constraintRect = CGSize(width: width, height: .greatestFiniteMagnitude)
        var attributes = attributedText?.attributes(at: 0, effectiveRange: nil)

        attributes?[NSAttributedString.Key.font] = font
        
        let string = NSAttributedString(string: fullText, attributes: attributes)
        
        let boundingBox = fullText.boundingRect(with: constraintRect, options: .usesLineFragmentOrigin, attributes: attributes, context: nil)
        
        // linescount
        let textStorage = NSTextStorage(attributedString: string)
        let layoutManager = NSLayoutManager()
        textStorage.addLayoutManager(layoutManager)
        
        var numberOfLines: Int = -1
        var index: Int = 0
        let numberOfGlyphs = UInt(layoutManager.numberOfGlyphs)
        
        let textContainer = NSTextContainer(size: constraintRect)
        textContainer.lineFragmentPadding = 0.0
        textContainer.lineBreakMode = .byWordWrapping
        
        layoutManager.addTextContainer(textContainer)
               
        var lineRange: NSRange = NSRange()
        while index < numberOfGlyphs {
            layoutManager.lineFragmentRect(
                forGlyphAt: index,
                effectiveRange: &lineRange)
            index = NSMaxRange(lineRange)
            numberOfLines += 1
        }
        
        let lineSpacingHeight = numberOfLines.cg * font.lineHeight * (lineSpacing - 1)
        
       
        return CGSize(width: boundingBox.width, height: boundingBox.height + lineSpacingHeight)
    }
    
    func getTextSize() -> Float {
        return font.pointSize.float
    }
    
    
    /**
     Resize the font to make the current text fit the label frame.
     - parameter maxFontSize:  The max font size available
     - parameter minFontScale: The min font scale that the font will have
     - parameter rectSize:     Rect size where the label must fit
     */
    public func fontSizeToFit(maxFontSize: CGFloat = 500, minFontScale: CGFloat = 0.1, rectSize: CGSize? = nil) {
        guard let unwrappedText = self.text else {
            return
        }
        
        let newFontSize = fontSizeThatFits(text: unwrappedText, maxFontSize: maxFontSize, minFontScale: minFontScale, rectSize: rectSize)
        font = font.withSize(newFontSize)
    }
    
    /**
     Returns a font size of a specific string in a specific font that fits a specific size
     - parameter text:         The text to use
     - parameter maxFontSize:  The max font size available
     - parameter minFontScale: The min font scale that the font will have
     - parameter rectSize:     Rect size where the label must fit
     */
    public func fontSizeThatFits(text string: String, maxFontSize: CGFloat = 500, minFontScale: CGFloat = 0.01, rectSize: CGSize? = nil) -> CGFloat {
        let maxFontSize = maxFontSize.isNaN ? 100 : maxFontSize
        let minFontScale = minFontScale.isNaN ? 0.1 : minFontScale
        let minimumFontSize = maxFontSize * minFontScale
        let rectSize = rectSize ?? bounds.size
        guard !string.isEmpty else {
            return self.font.pointSize
        }
        
        //let constraintSize = numberOfLines == 1 ?
        //CGSize(width: CGFloat.greatestFiniteMagnitude, height: rectSize.height) :
        let constraintSize = CGSize(width: rectSize.width, height: CGFloat.greatestFiniteMagnitude)
        let calculatedFontSize = binarySearch(string: string, minSize: minimumFontSize, maxSize: maxFontSize, size: rectSize, constraintSize: constraintSize)
        return (calculatedFontSize * 10.0).rounded(.down) / 10.0
    }
    
    
    
    private func currentAttributedStringAttributes() -> [NSAttributedString.Key: Any] {
        return attributedText!.attributes(at: 0, effectiveRange: nil)
    }
    
    
    
    private enum FontSizeState {
        case fit, tooBig, tooSmall
    }
    
    private func binarySearch(string: String, minSize: CGFloat, maxSize: CGFloat, size: CGSize, constraintSize: CGSize) -> CGFloat {
        let fontSize = (minSize + maxSize) / 2
        var attributes = currentAttributedStringAttributes()
        attributes[NSAttributedString.Key.font] = font.withSize(fontSize)
        let rect = string.boundingRect(with: constraintSize, options: .usesLineFragmentOrigin, attributes: attributes, context: nil)
        let state = numberOfLines == 1 ? singleLineSizeState(rect: rect, size: size) : multiLineSizeState(rect: rect, size: size)
        
        // if the search range is smaller than 0.1 of a font size we stop
        // returning either side of min or max depending on the state
        let diff = maxSize - minSize
        guard diff > 0.1 else {
            switch state {
            case .tooSmall:
                return maxSize
            default:
                return minSize
            }
        }
        
        switch state {
        case .fit: return fontSize
        case .tooBig: return binarySearch(string: string, minSize: minSize, maxSize: fontSize, size: size, constraintSize: constraintSize)
        case .tooSmall: return binarySearch(string: string, minSize: fontSize, maxSize: maxSize, size: size, constraintSize: constraintSize)
        }
    }
    
    private func singleLineSizeState(rect: CGRect, size: CGSize) -> FontSizeState {
        if rect.width >= size.width + 1 && rect.width <= size.width {
            return .fit
        } else if rect.width > size.width {
            return .tooBig
        } else {
            return .tooSmall
        }
    }
    
    private func multiLineSizeState(rect: CGRect, size: CGSize) -> FontSizeState {
        // if rect within 10 of size
        if rect.height < size.height + 1 &&
            rect.height > size.height - 1 &&
            rect.width > size.width + 1 &&
            rect.width < size.width - 1 {
            return .fit
        } else if rect.height > size.height || rect.width > size.width {
            return .tooBig
        } else {
            return .tooSmall
        }
    }
    
}

extension TextAlign {
    func toAlignment() -> NSTextAlignment  {
        switch self {
        case .left, .start:
            return NSTextAlignment.left
        case .end, .right:
            return NSTextAlignment.right
        default:
            return NSTextAlignment.center
        }
    }
}

extension NSAttributedString {
    func sizeFittingWidth(_ w: CGFloat) -> CGSize {
        let textStorage = NSTextStorage(attributedString: self)
        let size = CGSize(width: w, height: CGFloat.greatestFiniteMagnitude)
        let boundingRect = CGRect(origin: .zero, size: size)
        
        let textContainer = NSTextContainer(size: size)
        textContainer.lineFragmentPadding = 0
        
        let layoutManager = NSLayoutManager()
        layoutManager.addTextContainer(textContainer)
        
        textStorage.addLayoutManager(layoutManager)
        
        layoutManager.glyphRange(forBoundingRect: boundingRect, in: textContainer)
        
        let rect = layoutManager.usedRect(for: textContainer)
        
        return rect.integral.size
    }
}
