//
//  GenericTextLayoutApple.swift
//  iosApp
//
//  Created by rst10h on 18.08.22.
//

import Foundation
import SwiftUI
import shared
import UIKit

class GenericTextLayoutApple: InspUILabel, InnerGenericText, CircularText {
    
    static let canvasTextSize = 0.cg // probaly maximum 1024
    
    var fullText: String {
        didSet {
            super.text = fullText
            recompute()
            if (media.isCircularText()) {
                adjustCircularFontSize()
            }
            super.setNeedsDisplay()
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
    
    
    var templateWidth: Int32 = 0
    var templateHeight: Int32 = 0
    var circularGravity: TextAlign
    
    var paddingOffset: CGPoint = .zero
    
    lazy var tempTextAnimParam = DrawTextAnimParamApple()
    lazy var tempBackgroundAnimParam = DrawTextAnimParamApple()
    
    required init(media: MediaText) {
        self.media = media
        self.circularGravity = media.innerGravity
        self.needsRecompute = false
        self.fullText = media.text
        super.init(media.text)
        
        self.genericTextHelper = GenericTextHelper(media: media, layout: self)
        super.text = media.text
        super.numberOfLines = 0
        super.lineBreakMode = .byWordWrapping
        super.adjustsFontForContentSizeCategory = false
        super.backgroundColor = UIColor.clear
        
        
    }
    
    func refresh() {
        
    }
    
    func getLineForOffset(offset: Int32) -> Int32 {
        let line = textLines.lineNumberForCharIndex(charIndex: offset.int).int32
        return line
    }
    
    
    override func layoutSubviews() {
        super.layoutSubviews()
        recompute()
        refreshGradient()
        refreshBackgroundGradient()
        super.setNeedsDisplay()
        //frame size was changed
    }
    
    private var canvasGap: CGPoint = .zero
    
    func refreshCanvasGap() {
        if (Self.canvasTextSize == 0) { return }
        let w = (Self.canvasTextSize - textBounds.width) / 2
        let h = (Self.canvasTextSize - textBounds.height) / 2
        
        canvasGap = CGPoint(x: w, y: h)
    }
    
    private var textLines: TextLines = TextLines()
    
    func updateLinesCountForText() {
        guard let attributedText = attributedText else { return }
        let textStorage = NSTextStorage(attributedString: attributedText)
        let layoutManager = NSLayoutManager()
        textStorage.addLayoutManager(layoutManager)
        
        var numberOfLines: UInt = 0
        var index: UInt = 0
        let numberOfGlyphs = UInt(layoutManager.numberOfGlyphs)
        
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
        
        let textContainer = NSTextContainer(size: CGSize(width: isInWrapContent ? textBounds.width : frame.size.width, height: CGFloat.greatestFiniteMagnitude)) //hm, i don't remember why..
        //let textContainer = NSTextContainer(size: CGSize(width:  textBounds.width, height: CGFloat.greatestFiniteMagnitude)) //hm, i don't remember why..
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
    
    func drawTextBackgroundsSingle(animParam: TextAnimationParams, canvas: Any?, time: Double, out: Bool, shadowMode: Bool) {
        if (genericTextHelper?.lackBackground() == true) {
            
        }
        else {
            guard let parts: PartInfo  = genericTextHelper?.getPartInfo(animParam: animParam) else { return }
            let partsCount = parts.subParts?.count ?? 0
            for (partIndex, part) in parts.subParts!.enumerated() {
                
                let part = part as! PartInfo
                let index = part.index
                let partStartTime = part.startTime
                let lineNumber = getLineForOffset(offset: index)
                let fontHeight = font.lineHeight
                tempBackgroundAnimParam.nullify()
                tempBackgroundAnimParam.cornersRadius = media.radius
                
                var startIndex = fullText.index(fullText.startIndex, offsetBy: index.int)
                
                var endIndex = fullText.index(fullText.startIndex, offsetBy: index.int + part.length.int - 1)
                if (fullText[endIndex] == "\n") {
                    endIndex = fullText.index(fullText.startIndex, offsetBy: index.int + part.length.int - 2)
                }
                if (startIndex > endIndex) { return }
                let str = fullText[startIndex...endIndex] as NSString
                var r: CGRect
                if (needsUpdateCache || partBackgroundPositions[index.int] == nil) {
                    r = boundingRectForCharacterRange(NSRange(startIndex...endIndex, in: fullText))!
                    partBackgroundPositions[index.int] = r
                    
                } else {
                    r = partBackgroundPositions[index.int]!
                }
                
                
                let lineHeight = font.lineHeight * lineSpacing
                let linePosition = lineNumber.cg * lineHeight
                let paddingX = (textAlignment == .center && preferredMaxLayoutWidth <= textBounds.width) ? 0 : paddingOffset.x
                r.origin = CGPoint(x: r.minX + paddingX, y: paddingOffset.y + linePosition)
                
                tempBackgroundAnimParam.left = r.minX.float
                tempBackgroundAnimParam.top = r.minY.float
                tempBackgroundAnimParam.width = r.width.toInt32
                tempBackgroundAnimParam.height = r.height.toInt32
                tempBackgroundAnimParam.color = media.backgroundColor
                
                if (time >= partStartTime || out) {
                    genericTextHelper?.mayApplyBackAnimOut(
                        animParam: animParam,
                        backgroundAnimParam: tempBackgroundAnimParam,
                        shadowMode: shadowMode,
                        partIndex: partIndex.int32,
                        partsCount: partsCount.int32)
                    
                    genericTextHelper?.applyAnimation(textAnimationParams: animParam,
                                                      time: time,
                                                      partStartTime: partStartTime,
                                                      backgroundAnimParam: tempBackgroundAnimParam,
                                                      partIndex: partIndex.int32,
                                                      partsCount: partsCount.int32,
                                                      view: self, shadowMode: shadowMode, out: out
                    )
                    
                    if let rect = tempBackgroundAnimParam.clipRect?.cgRect.offsetBy(dx: r.minX, dy: r.minY) {
                        let color = backColor ?? UIColor(cgColor: media.backgroundColor.ARGB.cgColor!)
                        let p = UIBezierPath(roundedRect: rect, cornerRadius: media.view?.getCornerRadiusAbsolute().cg ?? 0)
                        color.withAlphaComponent(tempBackgroundAnimParam.alpha.cg).setFill()
                        p.fill()
                    } else {
                        let rect = CGRect(
                            x: tempBackgroundAnimParam.left.cg,
                            y: tempBackgroundAnimParam.top.cg,
                            width: tempBackgroundAnimParam.width.cg,
                            height: tempBackgroundAnimParam.height.cg)
                        
                        let color = backColor ?? UIColor(cgColor: media.backgroundColor.ARGB.cgColor!)
                        let p = UIBezierPath(roundedRect: rect, cornerRadius: media.view?.getCornerRadiusAbsolute().cg ?? 0)
                        color.setFill()
                        p.fill()
                    }
                }
                
            }
            
            
        }
        
        
    }
    
    var needsUpdateCache: Bool = true
    var partPositions: Dictionary<Int, CGRect> = [:]
    var partBackgroundPositions: Dictionary<Int, CGRect> = [:]
    
    
    func drawPart(canvas: Any?, time: Double, partInfo: PartInfo, textAnimationParams: TextAnimationParams, animatorOut: Bool, partIndex: Int32, partsCount: Int32, lineNumber: Int32) {
        
        let isTextAlwaysVisible = media.view?.templateParent.textViewsAlwaysVisible ?? false
        
        if (time >= partInfo.startTime || animatorOut || isTextAlwaysVisible) {
            if (circularRadius == 0) {
                drawSinglePart(time: time, partInfo: partInfo, textAnimationParams: textAnimationParams, animatorOut: animatorOut, partIndex: partIndex, partsCount: partsCount, lineNumber: lineNumber, isTextAlwaysVisible: isTextAlwaysVisible)
            } else {
                drawCircularPart(time: time, partInfo: partInfo, textAnimationParams: textAnimationParams, animatorOut: animatorOut, partIndex: partIndex, partsCount: partsCount, lineNumber: lineNumber, isTextAlwaysVisible: isTextAlwaysVisible)
            }
        }
    }
    private func drawCircularPart( time: Double, partInfo: PartInfo, textAnimationParams: TextAnimationParams, animatorOut: Bool, partIndex: Int32, partsCount: Int32, lineNumber: Int32, isTextAlwaysVisible: Bool) {
        
        let lineHeight = circularFont?.lineHeight ?? 10
        let charpos = circularCharPositions[partInfo.index.int]
        
        let context = UIGraphicsGetCurrentContext()!
        var point = CGPoint(x: circularRadius.cg - charpos.charWidth / 2, y: 0)
        context.saveGState()
        context.translateBy(x: bounds.midX, y: bounds.midY)
        context.rotate(by: Angle(degrees: charpos.charAngle.double).radians)
        context.translateBy(x: -bounds.midX, y: -bounds.midY)
        
        //======================== debug lines from char to center
        //            let lineWidth: CGFloat = 1.0
        //            context.setLineWidth(lineWidth)
        //            context.setStrokeColor(UIColor.red.cgColor)
        //            let startingPoint = CGPoint(x: bounds.width / 2, y: 0)
        //        let endingPoint = CGPoint(x: bounds.width / 2, y: bounds.width / 2)
        //            context.move(to: startingPoint )
        //            context.addLine(to: endingPoint )
        //            context.strokePath()
        //==========================================================
        
        tempTextAnimParam.initializeAndNullify(
            charSequence: fullText,
            isRtl: false,
            startTextToRender: partInfo.index,
            lengthTextToRender: partInfo.length,
            strokeWidth: 0,
            lineBaseline: 0,
            paddingTop: 0, lineTop: 0)
        
        tempTextAnimParam.height = lineHeight.toInt32
        tempTextAnimParam.left = point.x.float
        tempTextAnimParam.top = 0
        tempTextAnimParam.width = charpos.charWidth.toInt32
        tempTextAnimParam.color = media.textColor
        
        if (!isTextAlwaysVisible) {
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
        }
        
        point.x += tempTextAnimParam.translateX.cg
        point.y += tempTextAnimParam.translateY.cg
        
        var attributes = circularAttributes
        
        if let shadowColor = media.textShadowColor {
            if (media.textShadowBlurRadius?.intValue ?? 0 == 0) {
                let shadowC: UIColor = UIColor(cgColor: shadowColor.intValue.ARGB.cgColor!)
                attributes?[NSAttributedString.Key.foregroundColor] = shadowC
                let shadowPoint = CGPoint(x: point.x + (media.shadowOffsetX?.floatValue.cg ?? 0), y: point.y + (media.shadowOffsetY?.floatValue.cg ?? 0))
                
                (String(charpos.char) as NSString).draw(at: shadowPoint, withAttributes: attributes)
            }
        }
        
        if let shadowColors = media.shadowColors {
            if (media.textShadowBlurRadius?.intValue ?? 0 == 0) {
                let shadowC: UIColor = UIColor(cgColor: (shadowColors[0] as! KotlinInt).intValue.ARGB.cgColor!)
                attributes?[NSAttributedString.Key.foregroundColor] = shadowC
                let shadowPoint = CGPoint(x: point.x + (media.shadowOffsetX?.floatValue.cg ?? 0), y: point.y + (media.shadowOffsetY?.floatValue.cg ?? 0))
                (String(charpos.char) as NSString).draw(at: shadowPoint, withAttributes: attributes)
            }
        }
        
        
        let color = gradientColor ?? UIColor(cgColor: tempTextAnimParam.color.ARGB.cgColor!)
        if (media.strokeWidth == nil) {
            attributes?[NSAttributedString.Key.foregroundColor] = color.withAlphaComponent(tempTextAnimParam.alpha.cg)
        } else {
            attributes?[NSAttributedString.Key.foregroundColor] = UIColor.clear
            attributes?[NSAttributedString.Key.strokeColor] = color.withAlphaComponent(tempTextAnimParam.alpha.cg)
        }
        (String(charpos.char) as NSString).draw(at: point, withAttributes: attributes)
        
        context.restoreGState()
    }
    private func drawSinglePart(time: Double, partInfo: PartInfo, textAnimationParams: TextAnimationParams, animatorOut: Bool, partIndex: Int32, partsCount: Int32, lineNumber: Int32, isTextAlwaysVisible: Bool) {
        let index = partInfo.index
        //todo isRtl support??
        var startIndex = fullText.index(fullText.startIndex, offsetBy: index.int)
        var endIndex = fullText.index(fullText.startIndex, offsetBy: index.int + partInfo.length.int - 1)
        if (fullText[endIndex] == "\n") {
            endIndex = fullText.index(fullText.startIndex, offsetBy: index.int + partInfo.length.int - 2)
        }
        if (startIndex > endIndex) { return }
        let str = fullText[startIndex...endIndex] as NSString
        
        var r: CGRect
        if (needsUpdateCache || partPositions[index.int] == nil) {
            r = boundingRectForCharacterRange(NSRange(startIndex...endIndex, in: fullText))!
            
            partPositions[index.int] = r
            
        } else {
            r = partPositions[index.int]!
        }
        
        let paddingX = (textAlignment == .center && preferredMaxLayoutWidth <= textBounds.width) ? 0 : paddingOffset.x
        r.origin = CGPoint(x: r.minX + paddingX + canvasGap.x, y: paddingOffset.y + canvasGap.y)
        
        let lineHeight = font.lineHeight * lineSpacing
        let linePosition = lineNumber.cg * lineHeight
        let y_correction = linePosition //+ (textBounds.height - textHeight) / 2.cg
        r.size = CGSize(width: r.width, height: font.lineHeight)
        r = r.offsetBy(dx: 0, dy: y_correction)
        
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
        tempTextAnimParam.color = media.textColor
        
        //if (!isTextAlwaysVisible) {
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
        //}
        if (isTextAlwaysVisible) {
            tempTextAnimParam.translateX = 0
            tempTextAnimParam.translateY = 0
            tempTextAnimParam.rotate = 0
            tempTextAnimParam.clipRect = nil
            tempTextAnimParam.alpha = 1
        }
        r = r.offsetBy(dx: tempTextAnimParam.translateX.cg, dy: tempTextAnimParam.translateY.cg + 0)
        
        var attributes = attributedText!.attributes(at: 0, effectiveRange: nil)
        
        let context = UIGraphicsGetCurrentContext()!
        context.saveGState()
        
        let scaleDx: CGFloat = tempTextAnimParam.scaleX.cg * r.width - r.width
        let scaleDy: CGFloat = tempTextAnimParam.scaleY.cg * r.height - r.height
        
        context.translateBy(x: -scaleDx/2, y: -scaleDy/2)
        context.scaleBy(x: tempTextAnimParam.scaleX.cg, y: tempTextAnimParam.scaleY.cg)
        
        if let shadowColor = media.textShadowColor {
            if (media.textShadowBlurRadius?.intValue ?? 0 == 0) {
                let shadowC: UIColor = UIColor(cgColor: shadowColor.intValue.ARGB.cgColor!)
                attributes[NSAttributedString.Key.foregroundColor] = shadowC
                let shadowRect = r.offsetBy(dx: media.shadowOffsetX?.floatValue.cg ?? 0.cg, dy: media.shadowOffsetY?.floatValue.cg ?? 0.cg)
                
                str.draw(in: shadowRect, withAttributes: attributes)
            }
        }
        
        if let shadowColors = media.shadowColors {
            if (media.textShadowBlurRadius?.intValue ?? 0 == 0) {
                let shadowC: UIColor = UIColor(cgColor: (shadowColors[0] as! KotlinInt).intValue.ARGB.cgColor!)
                attributes[NSAttributedString.Key.foregroundColor] = shadowC
                let shadowRect = r.offsetBy(dx: (media.shadowOffsetX?.floatValue.cg ?? 0.cg)  * font.lineHeight, dy: (media.shadowOffsetY?.floatValue.cg ?? 0.cg)  * font.lineHeight)
                str.draw(in: shadowRect, withAttributes: attributes)
            }
        }
        
        
        let color = gradientColor ?? UIColor(cgColor: tempTextAnimParam.color.ARGB.cgColor!)
        if (media.strokeWidth == nil) {
            attributes[NSAttributedString.Key.foregroundColor] = color.withAlphaComponent(tempTextAnimParam.alpha.cg)
        } else {
            attributes[NSAttributedString.Key.foregroundColor] = UIColor.clear
            attributes[NSAttributedString.Key.strokeColor] = color.withAlphaComponent(tempTextAnimParam.alpha.cg)
        }
        
        if let cliprect = tempTextAnimParam.clipRect?.cgRect.offsetBy(dx: r.minX, dy: r.minY) {
            context.clip(to: [cliprect])
        }
        
        str.draw(with: r, options: [.usesDeviceMetrics, .usesFontLeading, .usesLineFragmentOrigin], attributes: attributes, context: nil)
        
        context.restoreGState()
        
    }
    
    private func calcTopOffsetForVerticalGravity() -> CGFloat {
        return (textBounds.height - textLines.linesCount.cg * font.lineHeight) / 2
    }
    
    func recompute() {
        partPositions.removeAll()
        partBackgroundPositions.removeAll()
        updateLinesCountForText()
        genericTextHelper?.recompute(text: text)
        needsUpdateCache = true
    }
    
    func draw(animParam: TextAnimationParams, canvas: Any?, time: Double, out: Bool) {
        genericTextHelper?.drawTextBackgrounds(animParam: animParam, canvas: nil, time: time, out: out)
        genericTextHelper?.drawParts(animParam: animParam, canvas: nil, time: time, out: out)
        if needsUpdateCache {
            needsUpdateCache = false
        }
    }
    
    
    override func drawText(in rect: CGRect) {
        genericTextHelper?.onDrawText(canvas: nil, currentFrame: currentFrame)
    }
    
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    func getTextFrame() -> CGSize {
        
        let attr = [NSAttributedString.Key.font: font]
        return (text as NSString).size(withAttributes: attr)
        
    }
    
    func getTextFrameWIthSize(maxWidth: CGFloat) -> CGSize {
        return text.labelSize(font: font, size: CGSize(width: maxWidth, height: CGFloat.greatestFiniteMagnitude))
    }
    
    var lineSpacing = 1.cg
    
    var letterSpacing = 0.cg
    
    func setLinespacing(newValue: Float) {
        self.lineSpacing = newValue.cg
    }
    
    func setLetterSpacing(value: Float? = nil) {
        //guard let text = text, !text.isEmpty else { return }
        if let newValue = value {
            letterSpacing = newValue.cg
        }
        guard let attributedText = attributedText else { return }
        let string = NSMutableAttributedString(attributedString: attributedText)
        let kernValue = font.pointSize * letterSpacing
        string.addAttribute(NSAttributedString.Key.kern, value: kernValue, range: NSRange(location: 0, length: string.length))
        self.attributedText = string
    }
    
    func setOutline() {
        
        guard
            let attributedText = attributedText,
            let strokeWidth = media.strokeWidth?.floatValue
        else { return }
        
        let string = NSMutableAttributedString(attributedString: attributedText)
        /**
         unlike android, ios automatically selects the thickness value depending on the size of the text, so here is a static value
         **/
        let value = Constants.TEXT_OUTLINE_WIDTH
        let strokeColor: UIColor = gradientColor ?? textColor //UIColor(cgColor: media.textStrokeColor?.intValue.ARGB.cgColor!)
        string.addAttribute(NSAttributedString.Key.strokeWidth, value: value, range: NSRange(location: 0, length: string.length))
        string.addAttribute(NSAttributedString.Key.strokeColor, value: strokeColor, range: NSRange(location: 0, length: string.length))
        super.textColor = .clear
        self.attributedText = string
    }
    
    private(set) var fontSize: CGFloat = 10 {
        didSet {
            setFont()
            
        }
    }
    
    func setupShadow() {
        if let shadowBlurRadius = media.textShadowBlurRadius {
            let shadowColors = (media.shadowColors?[0] as? KotlinInt) ?? media.textShadowColor
            let shadowC: UIColor = UIColor(cgColor: shadowColors?.intValue.ARGB.cgColor ?? UIColor.black.cgColor) //only one shadow color supported right now
            let shadowOffset =  CGSize(width: (media.shadowOffsetX?.floatValue.cg ?? 0.cg)  * font.lineHeight, height: (media.shadowOffsetY?.floatValue.cg ?? 0.cg)  * font.lineHeight)
            let shadowRadius = shadowBlurRadius.floatValue.cg / Constants.TEXT_SHADOW_BLUR_DELIMITER * font.lineHeight
            let shadowColor = shadowC.cgColor
            super.layer.cornerRadius = shadowRadius / 3;
            super.layer.shadowOffset = shadowOffset
            super.layer.shadowRadius = shadowRadius
            super.layer.shadowOpacity = 1;
            super.layer.shadowColor = shadowColor
        }
        else {
            super.layer.shadowOpacity = 0
            super.layer.shadowColor = UIColor.clear.cgColor
        }
        
    }
    
    func setTextSize(newSize: CGFloat) {
        fontSize = newSize
        if (circularRadius > 0) {
            adjustCircularFontSize()
        }
    }
    
    func setFont(font: UIFont? = nil) {
        let font = font ?? self.font
        self.font = font.withSize(fontSize)
        setLetterSpacing()
        DispatchQueue.main.async {
            self.setupShadow()
        }
        setOutline()
        updateLinesCountForText()
        recompute()
        
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
    
    /**
     Circular text logic
     - parameter    circularRadius: text is circular if it is not 0
     */
    
    var circularRadius: Float = 0
    var circularTextSize: Float = 0
    var circularFont: UIFont? = nil
    var circularCharPositions: [CircularCharPosition] = []
    var circularAttributes: [NSAttributedString.Key : Any]? = nil
    
    func adjustCircularFontSize() {
        var attributes = attributedText?.attributes(at: 0, effectiveRange: nil)
        var circleLength = -1.cg
        var textWidth = 0.cg
        var fontHeight = font.lineHeight
        var fontSize = font.pointSize
        let constraintRect = CGSize(width: .greatestFiniteMagnitude, height: fontHeight)
        while (textWidth > circleLength) {
            circleLength = (circularRadius.cg - fontHeight) * 2 * CGFloat.pi
            
            attributes?[NSAttributedString.Key.font] = font.withSize(fontSize)
            attributes?[NSAttributedString.Key.kern] = 0
            let boundingBox = fullText.boundingRect(with: constraintRect, options: [], attributes: attributes, context: nil)
            textWidth = boundingBox.width
            
            if (textWidth > circleLength) {
                fontSize -= 1
                print("circular text width = \(textWidth) fontSize = \(fontSize)")
            }
        }
        circularFont = font.withSize(fontSize)
        circularAttributes = attributes
        print("circularText: circle length \(circleLength) textWidth \(textWidth) textSize \(fontSize)")
        calculateCircularCharsPositions()
    }
    
    func calculateCircularCharsPositions() {
        guard
            let fontHeight = circularFont?.lineHeight,
            let circularFont = circularFont
        else { return }
        
        var attributes = attributedText?.attributes(at: 0, effectiveRange: nil)
        attributes?[NSAttributedString.Key.kern] = 0 //todo char spacing?
        attributes?[NSAttributedString.Key.font] = circularFont
        let constraintRect = CGSize(width: .greatestFiniteMagnitude, height: fontHeight)
        var index = 0
        var lastAngle: Float = 0
        circularCharPositions.removeAll()
        print("circular calculating ----------------")
        var prevWidth = 0.cg
        var lineWidth = 0.cg
        fullText.forEach{ char in
            let charSize = (String(char) as NSString).boundingRect(with: constraintRect, options: [], attributes: attributes, context: nil)
            let lengthDelta = prevWidth / 2 + charSize.width / 2
            lineWidth += lengthDelta
            let angleDelta = InspMathUtil().getArcAngle(radius: circularRadius - fontHeight.float, arcLength: lineWidth.float)
            circularCharPositions.append(CircularCharPosition(char: char, charWidth: charSize.width, charAngle: angleDelta))
            //print("circular item \(circularCharPositions.last) length = \(lineWidth)")
            prevWidth = charSize.width
            lastAngle += angleDelta
        }
    }
    
    func labelSize(withConstrainedWidth width: CGFloat, font: UIFont) -> CGSize {
        let constraintRect = CGSize(width: width, height: .greatestFiniteMagnitude)
        var attributes = attributedText?.attributes(at: 0, effectiveRange: nil)
        
        attributes?[NSAttributedString.Key.font] = font
        attributes?[NSAttributedString.Key.kern] = font.pointSize * letterSpacing
        
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
        
        let newFontSize = fontSizeThatFits(text: text, maxFontSize: maxFontSize, minFontScale: minFontScale, rectSize: rectSize)
        font = font.withSize(newFontSize)
    }
    
    /**
     Returns a font size of a specific string in a specific font that fits a specific size
     - parameter text:         The text to use
     - parameter maxFontSize:  The max font size available
     - parameter minFontScale: The min font scale that the font will have
     - parameter rectSize:     Rect size where the label must fit
     */
    public func fontSizeThatFits(text string: String, maxFontSize: CGFloat = 500, minFontScale: CGFloat = 0.0001, rectSize: CGSize? = nil) -> CGFloat {
        let maxFontSize = maxFontSize.isNaN ? 100 : maxFontSize
        let minFontScale = minFontScale.isNaN ? 0.0001 : minFontScale
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
        let kern = fontSize * letterSpacing
        var attributes = currentAttributedStringAttributes()
        let style = NSMutableParagraphStyle()
        style.lineHeightMultiple = lineSpacing
        style.alignment = textAlignment
        attributes[NSAttributedString.Key.font] = font.withSize(fontSize)
        attributes[NSAttributedString.Key.kern] = kern
        let rect = labelSize(withConstrainedWidth: constraintSize.width, font: font.withSize(fontSize))
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
    
    private func singleLineSizeState(rect: CGSize, size: CGSize) -> FontSizeState {
        if rect.width >= size.width + 1 && rect.width <= size.width {
            return .fit
        } else if rect.width > size.width {
            return .tooBig
        } else {
            return .tooSmall
        }
    }
    
    private func multiLineSizeState(rect: CGSize, size: CGSize) -> FontSizeState {
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
    
    
    var gradientColor: UIColor? = nil
    
    var backColor: UIColor? = nil
    
    private(set) var gradientLayer: CAGradientLayer? = nil
    private(set) var backgroundGradientLayer: CAGradientLayer? = nil
    
    func setNewBackgroundGradientLayer(gradientLayer: CAGradientLayer, fromUser: Bool) {
        self.backgroundGradientLayer = gradientLayer
        if (fromUser) {
            refreshBackgroundGradient()
            self.setNeedsDisplay()
        }
    }
    
    func setNewGradientLayer (gradientLayer: CAGradientLayer, fromUser: Bool) {
        self.gradientLayer = gradientLayer
        if (fromUser) {
            refreshGradient()
            self.setNeedsDisplay()
        }
    }
    private func refreshBackgroundGradient() {
        guard let gradientLayer = self.backgroundGradientLayer else { return }
        gradientLayer.frame = self.frame
        
        UIGraphicsBeginImageContext(textBounds.size)
        gradientLayer.render(in: UIGraphicsGetCurrentContext()!)
        let image = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        backColor = UIColor(patternImage: image!)
    }
    
    private func refreshGradient() {
        guard let gradientLayer = self.gradientLayer else { return }
        gradientLayer.frame = self.frame
        
        UIGraphicsBeginImageContext(textBounds.size)
        gradientLayer.render(in: UIGraphicsGetCurrentContext()!)
        let image = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        gradientColor = UIColor(patternImage: image!)
    }
    
    func removeGradientColor() {
        gradientLayer = nil
        gradientColor = nil
        DispatchQueue.main.async {
            self.setNeedsDisplay()
        }
    }
    
    func removeBackgroundGradient() {
        backColor = nil
        backgroundGradientLayer = nil
        DispatchQueue.main.async {
            self.setNeedsDisplay()
        }
    }
    deinit {
        print("deinit generic text layout \(text)")
    }
}
