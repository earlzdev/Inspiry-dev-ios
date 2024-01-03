//
//  ViewProviderApple.swift
//  iosApp
//
//  Created by rst10h on 28.01.22.
//

import Foundation
import shared

class InspViewProvider: ViewProvider {
    
    
    func getInspGroupView(media: MediaGroup, parentInsp: InspParent, unitsConverter: BaseUnitsConverter, templateView: InspTemplateView, fontsManager: FontsManager, loggerGetter: LoggerGetter, movableTouchHelperFactory: MovableTouchHelperFactory) -> InspGroupView {
        
        let animationHelper = AnimationHelperApple(media: media)
        
        let viewPlatform = ViewPlatformApple()
        
        let inspView = InspGroupView(
            media: media,
            parentInsp: parentInsp,
            view: viewPlatform,
            unitsConverter: unitsConverter,
            animationHelper: animationHelper,
            loggerGetter: loggerGetter,
            touchHelperFactory: movableTouchHelperFactory,
            templateParent: templateView)
        
        animationHelper.inspView = inspView.asGeneric()
        
        return inspView
    }
    
    func getInspMediaView(media: MediaImage, parentInsp: InspParent, unitsConverter: BaseUnitsConverter, templateView: InspTemplateView, fontsManager: FontsManager, loggerGetter: LoggerGetter, movableTouchHelperFactory: MovableTouchHelperFactory) -> InspMediaView {
        let animationHelper = AnimationHelperApple(media: media)
        let viewPlatform = ViewPlatformApple()
        let innerView = InnerMediaViewApple()
        let inspView = InspMediaView(
            media: media,
            parentInsp: parentInsp,
            view: viewPlatform,
            unitsConverter: unitsConverter,
            animationHelper: animationHelper,
            loggerGetter: loggerGetter,
            innerMediaView: innerView,
            touchHelperFactory: movableTouchHelperFactory,
            templateParent: templateView,
            fileSystem: OkioFileSystem.companion.SYSTEM
        )
        
        animationHelper.inspView = inspView.asGeneric()
        innerView.mediaView = inspView
        
        return inspView
    }
    
    func getInspPathView(media: MediaPath, parentInsp: InspParent, unitsConverter: BaseUnitsConverter, templateView: InspTemplateView, fontsManager: FontsManager, loggerGetter: LoggerGetter, movableTouchHelperFactory: MovableTouchHelperFactory) -> InspPathView {
        let animationHelper = AnimationHelperApple(media: media)
        let viewPlatform = ViewPlatformApple()
        
        let innerPathView = InnerPathViewApple(media: media)
        
        let inspView = InspPathView(
            media: media,
            parentInsp: parentInsp,
            view: viewPlatform,
            unitsConverter: unitsConverter,
            animationHelper: animationHelper,
            path: ApplePath(),
            innerViewPath: innerPathView,
            loggerGetter: loggerGetter,
            touchHelperFactory: movableTouchHelperFactory,
            templateParent: templateView
        )
        
        innerPathView.drawPath = {
            return inspView.drawPath() as? ApplePath
        }
        
        animationHelper.inspView = inspView.asGeneric()
        return inspView
    }
    
    func getInspTextView(media: MediaText, parentInsp: InspParent, unitsConverter: BaseUnitsConverter, templateView: InspTemplateView, fontsManager: FontsManager, loggerGetter: LoggerGetter, movableTouchHelperFactory: MovableTouchHelperFactory) -> InspTextView {
        let animationHelper = AnimationHelperApple(media: media)
        let viewPlatform = ViewPlatformApple()
        let innerTextHolder = InnerTextHolderApple(media: media, viewPlatform: viewPlatform)
        let inspView = InspTextView(
            media: media,
            parentInsp: parentInsp,
            view: viewPlatform,
            unitsConverter: unitsConverter,
            animationHelper: animationHelper,
            fontsManager: fontsManager,
            textView: innerTextHolder,
            loggerGetter: loggerGetter,
            touchHelperFactory: movableTouchHelperFactory,
            templateParent: templateView)
        
        animationHelper.inspView = inspView.asGeneric()
        innerTextHolder.textView?.setDurationSource { [weak inspView] in
            return KotlinInt(int: inspView?.duration ?? 0)
        }
        innerTextHolder.textView?.setStartTimeSource { [weak inspView] in
            return KotlinInt(int: inspView?.getStartFrameShortCut() ?? 0)
        }
        
        return inspView
    }
    
    func getInspVectorView(media: MediaVector, parentInsp: InspParent, unitsConverter: BaseUnitsConverter, templateView: InspTemplateView, fontsManager: FontsManager, loggerGetter: LoggerGetter, movableTouchHelperFactory: MovableTouchHelperFactory) -> InspVectorView {
        let animationHelper = AnimationHelperApple(media: media)
        let viewPlatform = ViewPlatformApple()
        let innerView = InnerVectorViewApple(media: media)
        
        let inspView = InspVectorView(
            media: media,
            parentInsp: parentInsp,
            view: viewPlatform,
            unitsConverter: unitsConverter,
            animationHelper: animationHelper,
            innerVectorView: innerView,
            viewFps: FrameConstantsKt.FPS,
            loggerGetter: loggerGetter,
            touchHelperFactory: movableTouchHelperFactory,
            templateParent: templateView
        )
        animationHelper.inspView = inspView.asGeneric()
        return inspView
        
    }
    
    func getInspVideoView(media: MediaImage, parentInsp: InspParent, unitsConverter: BaseUnitsConverter, templateView: InspTemplateView, fontsManager: FontsManager, loggerGetter: LoggerGetter, movableTouchHelperFactory: MovableTouchHelperFactory) -> InspSimpleVideoView {
        
        let viewPlatform = ViewPlatformApple()
        let animationHelper = AbsAnimationHelperKt.getEmptyAnimationHelper()
        
        let inspView = InspSimpleVideoView(
            media: media,
            parentInsp: parentInsp,
            view: viewPlatform,
            unitsConverter: unitsConverter,
            animationHelper: animationHelper,
            loggerGetter: loggerGetter,
            movableTouchHelperFactory: movableTouchHelperFactory,
            getPlayer: {
                VideoPlayerApple()
            },
            templateParent: templateView)
        
        return inspView
    }
}

