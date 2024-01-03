//
//  InnerVectorViewApple.swift
//  iosApp
//
//  Created by rst10h on 28.01.22.
//

import Foundation
import shared
import SwiftUI
import Lottie
import SVGKitSwift
import SVGKit

class InnerVectorViewApple: InnerVectorView, ObservableObject {
    
    var lottieFrame: Int32 = 0 {
        didSet {
            onFrameChanged(frame: lottieFrame.int)
        }
    }
    
    /**
     CoreAnimation is almost twise as fast than mainThread
     however  not all animations are supported, so there is an automatic mode that switches to mainThread if necessary
     */
    
    let lottieView = LottieAnimationView(configuration: LottieConfiguration(renderingEngine: .automatic))
    var svgView: SVGKImageView? = nil
    
    var onFailedToInitialize: ((KotlinThrowable?) -> Void)? = nil
    
    var onInitialized: ((KotlinFloat, KotlinInt) -> Void)? = nil
    
    var viewFps: Int32 = 30
    
    var media: MediaVector
    
    var container: InspVectorView? = nil
    
    init (media: MediaVector) {
        self.media = media
    }
    
    func onLottieLoaded(fps: Float, duration: Int) {
        if let action = onInitialized {
            action(KotlinFloat(value: fps), KotlinInt(integerLiteral: duration))
        }
        if ( container?.templateParent.templateMode == .edit) {
            onFrameChanged(frame: 0)
        }
    }
    
    func clearDisplayResource() {
        
    }
    
    var prevFrame = -1
    func onFrameChanged(frame: Int) {
        DispatchQueue.main.async { [self] in
            
            
            let isPlaying = (container?.templateParent.isPlaying.value as? KotlinBoolean)?.boolValue == true
            let isInEditMode = container?.templateParent.templateMode == .edit && !isPlaying
            if (prevFrame == frame && !isInEditMode) {
                return
            }
            if let anim = lottieView.animation {
                if (isInEditMode) {
                    if (lottieView.isAnimationPlaying) { lottieView.stop() }
                    let staticFrame = isPlaying ? anim.endFrame : frame.cg
                    let useStaticFrame = (frame.cg > anim.endFrame && media.isLoopEnabled != true) || isInEditMode
                    self.lottieView.currentFrame = useStaticFrame ? staticFrame : frame.cg
                } else {
//                    if (frame == 0 && media.startFrame == container?.currentFrame) {
//                        self.lottieView.play(fromProgress: 0, toProgress: 1, loopMode: (media.isLoopEnabled == KotlinBoolean(bool: true)) ? .loop : .playOnce, completion: nil)
//                    } else {
                        prevFrame = frame
                        let lastFrame = frame.cg > anim.endFrame && media.isLoopEnabled != true
                        self.lottieView.currentFrame = lastFrame ? anim.endFrame : frame.cg
//                    }
                }
            }
        }
    }
    
    func loadAnimation(originalSource: String) {
        
    }
    
    func loadAnimation(originalSource: String, reduceBlur: Bool) {
        
    }
    
    func loadAnimation(originalSource: String, isLottieAnimEnabled: Bool, reduceBlur: Bool) {
        container = media.view?.asInspVectorView()
        let asset = ResourceContainerExtKt.getAssetByFilePath(MR.assets(), filePath: originalSource.removeSheme())
        DispatchQueue.global(qos: .background).async { [self] in

            let anim = LottieAnimation.named(asset.fileName, bundle: asset.bundle, animationCache: nil) //todo for usinc animationCache need to make unique names, right?
            DispatchQueue.main.async { [self] in
                lottieView.animation = anim
                lottieView.contentMode = media.scaleType == .fitXy ? .scaleToFill : .scaleAspectFit
                lottieView.loopMode = (media.isLoopEnabled == true) ? LottieLoopMode.loop : LottieLoopMode.playOnce
                lottieView.setContentCompressionResistancePriority(.fittingSizeLevel, for: .horizontal)
                lottieView.setContentCompressionResistancePriority(.fittingSizeLevel, for: .vertical)
                

                
                if let it = lottieView.animation {
                    let fps = it.framerate
                    let duration = ((it.endFrame - it.startFrame).float.double * 30.0 / fps).rounded()
                    onLottieLoaded(fps: Float(truncating: KotlinFloat(floatLiteral: fps)), duration: Int(duration))
                }
                
            }
        }
    }
    
    func loadSvg(originalSource: String) {
        DispatchQueue.main.async { [self] in
            print("load svg \(originalSource)")
            let asset = ResourceContainerExtKt.getAssetByFilePath(MR.assets(), filePath: originalSource.removeSheme())
            let svgkImage = SVGKImage(named: asset.fileName, in: asset.bundle)
            svgView = SVGKFastImageView(svgkImage: svgkImage)
            svgView?.contentMode = .scaleAspectFit
            svgView?.setContentCompressionResistancePriority(.fittingSizeLevel, for: .horizontal)
            svgView?.setContentCompressionResistancePriority(.fittingSizeLevel, for: .vertical)
            onInitialized?(0,0)
        }
    }
    
    private var keyPathWasChanged = false
    func resetColorKeyPath(key: KotlinArray<NSString>) {
        guard keyPathWasChanged && container?.templateMode == .edit else { return }
        var keystring = [String]()
        for i in 0..<key.size {
            keystring.append(key.get(index: i)! as String)
        }
        keystring.append("Color")
        let keypath = AnimationKeypath(keys: keystring)
        lottieView.removeValueProvider(keypath: keypath)

    }
    
    func setColorFilter(color: KotlinInt?) {
        
    }
    
    func setColorKeyPath(color: Int32, key: KotlinArray<NSString>) {
        keyPathWasChanged = true
        var keystring = [String]()
        for i in 0..<key.size {
            keystring.append(key.get(index: i)! as String)
        }
        keystring.append("Color")
        let valueProvider = ColorValueProvider(color.toLottieColor())
        let keypath = AnimationKeypath(keys: keystring)
        lottieView.setValueProvider(valueProvider, keypath: keypath)
    }
    
    func setGradientKeyPath(gradient: PaletteLinearGradient, key: KotlinArray<NSString>) {
        
    }
    
    func setScaleType(scaleType: ScaleType) {
        
    }
    
}
