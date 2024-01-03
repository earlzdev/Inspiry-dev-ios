//
//  InnerTextHolderApple.swift
//  iosApp
//
//  Created by rst10h on 28.01.22.
//

import Foundation
import shared
import UIKit
import Macaw

class InnerTextHolderApple: InnerTextHolder {
    
    let media: MediaText
    var textView: GenericTextLayoutApple?
    let viewPlatform: ViewPlatformApple
    let unitsConverter: BaseUnitsConverter = Dependencies.resolveAuto()
    let fontObtainer: PlatformFontObtainerImpl = Dependencies.diContainer.resolveAuto()
    
    var text: String {
        didSet {
            textView?.fullText = text
        }
    }
    
    init(media: MediaText, viewPlatform: ViewPlatformApple) {
        self.currentFrame = 0
        self.media = media
        self.viewPlatform = viewPlatform
        self.textView = GenericTextLayoutApple(media: media)
        self.text = media.text
        textView?.setAlignment(media.innerGravity)
        textView?.setLinespacing(newValue: media.lineSpacing)
        updateCircularTextRadius()
        setFont(data: media.font)
        textView?.setLetterSpacing(value: media.letterSpacing)
        initColors()
    }
    
    //for circular text
    func adjustTextSize(newTextSize: Float, action: @escaping (KotlinFloat) -> Void) {
        
    }
    
    
    func calcDurations(includeStartTimeToOut: Bool) -> KotlinPair<KotlinInt, KotlinInt> {
        return textView?.genericTextHelper?.calcDurations(includeStartTimeToOut: includeStartTimeToOut) ?? KotlinPair(first: 0, second: 0)
    }
    
    
    func doOnInnerTextLayout(action: @escaping () -> Void) {
        textView?.recompute() //todo?
        DispatchQueue.main.async {
            action() //todo?
        }
    }
    
    private var textGradient: PaletteLinearGradient? = nil
    
    private func initColors() {
        if let gradient = media.textGradient {
            textView?.setNewGradientLayer(gradientLayer: gradient.getCAGradientLayer(), fromUser: false)
        } else {
            setNewTextColor(color: media.textColor)
        }
        if let back = media.backgroundGradient {
            textView?.setNewBackgroundGradientLayer(gradientLayer: back.getCAGradientLayer(), fromUser: false)
        }
    }
    
    func onColorChanged() {
        if let gradient = media.textGradient {
            textView?.setNewGradientLayer(gradientLayer: gradient.getCAGradientLayer(), fromUser: true)
        } else {
            setNewTextColor(color: media.textColor)
        }
        if let back = media.backgroundGradient {
            textView?.setNewBackgroundGradientLayer(gradientLayer: back.getCAGradientLayer(), fromUser: true)
        } else {
            textView?.removeBackgroundGradient()
        }
        textView?.setupShadow()
    }
    
    private var templateSize: CGSize = .zero
    
    func layoutUpdate() {
        guard let textView = textView else { return }
        let width = templateSize.width.toInt32
        let height = templateSize.height.toInt32
        
        var size: CGFloat = unitsConverter.convertUnitToPixelsF(value: media.textSize, screenWidth: width, screenHeight: height, fallback: 0, forHorizontal: true).cg
        
        if (media.layoutPosition.height == "wrap_content") {
            if (media.layoutPosition.width == "wrap_content") {
                textView.preferredMaxLayoutWidth = width.cg
            } else {
                textView.preferredMaxLayoutWidth = viewPlatform.width.cg
            }
            
            
            let frame = textView.labelSize(withConstrainedWidth: textView.preferredMaxLayoutWidth, font: textView.font.withSize(size))
            
            let w = frame.width.toInt32 + viewPlatform.paddingRight + viewPlatform.paddingLeft
            let h = frame.height.toInt32 + viewPlatform.paddingTop + viewPlatform.paddingBottom
            
            viewPlatform.width = w
            viewPlatform.height = h
            
            if media.view?.parentInsp != nil {
                viewPlatform.resolveLayoutParams(lp: media.layoutPosition, parentSize: CGSize(width: width.cg, height: height.cg), unitsConverter: unitsConverter, templateSize: CGSize(width: width.cg, height: height.cg))
                
            } else {
                viewPlatform.resolveLayoutParams(lp: media.layoutPosition, parentSize: templateSize, unitsConverter: unitsConverter, templateSize: templateSize)
            }
            
            textView.textBounds = CGRect(origin: CGPoint(x: 0, y: 0), size: frame)
            textView.paddingOffset = CGPoint(x: viewPlatform.paddLeft, y: viewPlatform.paddTop)
            updateCircularTextRadius()
        } else {
            let frameSize = viewPlatform.paddingBounds()
            textView.paddingOffset = CGPoint(x: viewPlatform.paddLeft, y: viewPlatform.paddTop)
            textView.preferredMaxLayoutWidth = frameSize.width
            textView.textBounds = frameSize
            updateCircularTextRadius()
            size = textView.fontSizeThatFits(text: text, rectSize: frameSize.size)
            
            //textSize_ = size.float
            
            
        }
        textView.setTextSize(newSize: size)
        if let parentGroup = media.view?.parentInsp as? InspGroupView {
            if (parentGroup.media.layoutPosition.isInWrapContentMode()) {
                parentGroup.onChildSizeChanged(newSize: CGSize(width: viewPlatform.width.cg, height: viewPlatform.height.cg))
            }
        }
        textView.refreshCanvasGap()
        self.textView?.setNeedsDisplay()
    }
    
    //template size changed, params: template width, template height
    func onParentSizeChanged(width: Int32, height: Int32) {
        
        
        if (width == 0 || height == 0) {
            return
        }
        
        templateSize.width = width.cg
        templateSize.height = height.cg
        
        if (media.view?.templateParent.templateMode == .edit) {
            viewPlatform.onInvalidate {
                DispatchQueue.main.async {
                    self.textView?.setNeedsDisplay()
                }
            }
        } else {
            viewPlatform.onInvalidate(action: nil)
        }
        
        
        layoutUpdate()
        
        
        
        
    }
    
    func onTextAlignmentChange(align: TextAlign) {
        textView?.setAlignment(align)
        textView?.setNeedsLayout()
    }
    
    func refresh() {
        
    }
    
    func requestLayout() {
        layoutUpdate()
    }
    
    func setFont(data: FontData?) {
        if let font = try? fontObtainer.getTypefaceFromFontData(fontData: data) {
            textView?.setFont(font: font)
        }
        layoutUpdate()
        
    }
    
    func setLineSpacing(spacing: Float) {
        textView?.setLinespacing(newValue: spacing)
    }
    
    func setNewTextColor(color: Int32) {
        //DispatchQueue.main.async {
        textView?.removeGradientColor()
        self.textView?.textColor = UIColor(color.ARGB)
        //}
        
    }
    
    func setOnClickListener(onClick: (() -> Void)?) {
        if let onClick = onClick {
            textView?.setOnClickListener {
                onClick()
                if let tv = self.media.view?.asInspTextView() {
                    if (!tv.isSelectedForEdit) {
                        tv.setSelected()
                    }
                }
            }
        } else {
            textView?.removeOnClickListener()
        }
    }
    
    func setPadding(layoutPosition: LayoutPosition, parentWidth: Int32, parentHeight: Int32, unitsConverter: BaseUnitsConverter, additionalPad: Int32) {
        
    }
    
    func switchToAutoSizeMode() {
        print("to autosize")
    }
    
    func switchToWrapContentMode() {
        layoutUpdate()
    }
    
    func updateCircularTextRadius() {
        guard let textView = textView,
        media.isCircularText()
        else { return }
        textView.circularRadius = textView.textBounds.width.float / 2
    }
    
    func updateCircularTextSize(textSize: Float) {
        
    }
    
    var currentFrame: Int32 {
        didSet {
            textView?.currentFrame = currentFrame
        }
    }
    
    var durationIn: Int32 {
        get {
            return textView?.genericTextHelper?.durationIn ?? 0
        }
    }
    
    var durationOut: Int32 {
        get {
            return textView?.genericTextHelper?.durationOut ?? 0
        }
    }
    
    var letterSpacing: Float = 1 {
        didSet {
            textView?.setLetterSpacing(value: letterSpacing)
        }
    }
    
    var onTextChanged: (String) -> Void = { text in
        //todo
    }
    
    var radius: Float = 0
    
    private var fontSize: Float? = nil
    
    var textSize_: Float {
        get {
            textView?.getTextSize() ?? 0
        }
        set {
            //fontSize = newValue
            //textView?.setTextSize(newSize: newValue.cg)
        }
    }
    deinit {
        print("deinit text")
        textView?.genericTextHelper = nil
        textView = nil
    }
}
