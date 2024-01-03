//
//  InnerMediaViewApple.swift
//  iosApp
//
//  Created by rst10h on 28.01.22.
//

import Foundation
import shared
import SwiftUI
import Kingfisher
import AVFoundation

class InnerMediaViewApple: InnerMediaView {
    func setRecording(value: Bool) {
        isRecording = value
    }
    
    var isRecording: Bool = false
    
    var localUrl: Bool = false
    var blurRadius: CGFloat = 0
    
    let imageView: UIImageView = {
        let iv = UIImageView(frame: .zero)
        iv.contentMode = .scaleAspectFill
        return iv
    }()
    
    let playerView: TemplateVideoPlayer = TemplateVideoPlayer()
    
    let mediaFrame: InspUIView = InspUIView(frame: .zero)
    
    let touchTransformHelper = MediaTransformHelper()
    
    weak var mediaView: InspMediaView? = nil {
        didSet {
            touchTransformHelper.media = mediaView?.media
        }
    }
    
    
    private var _onSizeKnownActions: [(() -> Void)] = []
    
    func sizeWasChanged() {
        for action in _onSizeKnownActions {
            action()
        }
        _onSizeKnownActions.removeAll()
    }
    
    func doWhenSizeIsKnown(function: @escaping () -> Void) {
        if (mediaView?.viewWidth == 0 || mediaView?.viewHeight == 0) {
            _onSizeKnownActions.append(function)
        } else {
            function()
        }
    }
    
    private var waitToPlay = false
    
    private func canPlay(frame: Int) -> Bool {
        let startFrame = mediaView!.media.startFrame

        return frame >= startFrame && mediaView?.templateParent.isPlayingActive == true
    }
    
    private func getOffsetMsForFrame(frame: Int) -> Int {
        let videoFrame = frame - mediaView!.media.startFrame.int
        let timeMs = Int(videoFrame.double * FrameConstantsKt.FRAME_IN_MILLIS + mediaView!.getVideoStartTimeMs().double)
        
        if (timeMs < 0) {
            return mediaView!.getVideoStartTimeMs()
        }
        var endTime = Int64(mediaView!.getVideoEndTimeMs())
        if (endTime == 0 && mediaView!.media.isEditable == false) { endTime = mediaView!.getVideoDurationMs() }
        if (timeMs > endTime) {
            if (mediaView?.media.isLoopEnabled == true) {
                let startTime = Int64(mediaView!.getVideoStartTimeMs())
                let duration = endTime - startTime
                let currentTime = videoFrame.double * FrameConstantsKt.FRAME_IN_MILLIS
                let realTime = Int64(currentTime) % duration + startTime
                return Int(realTime)
            } else {
                return mediaView!.getVideoEndTimeMs()
            }
        }
        
        return timeMs
        
    }
    
    func maySeekToStartOrEnd(frame: Int) {
        
        let videoCurrentTimeMs = getOffsetMsForFrame(frame: frame).double
        seekTo(timeMs: Int(videoCurrentTimeMs))
    }
    
    func drawVideoFrameAsync(frame: Int32, sequential: Bool) {
        
        guard mediaView?.isVideo() == true else { return }
        
        if (!isRecording) {
            if (playerView.isPlaying() == false && !waitToPlay) {
                maySeekToStartOrEnd(frame: frame.int)
                
            }
            
            if (waitToPlay) {
                if (canPlay(frame: frame.int)) {
                    waitToPlay = false
                    print("start video playing \(mediaView?.media.id) dup \(mediaView?.media.duplicate)")
                    startVideoPlaying()
                }
            }
        } else {
            //todo move to sync
            
        }
    }
    private var lastTime = -1
    func drawVideoFrameSync(frame: Int32, sequential: Bool) {
        guard let mediaView = mediaView else {
            return
        }
        guard mediaView.isVideo() == true else { return }
        
        if (isRecording) {
            let maxVisibleDuration = Int32(mediaView.getDurationForTrimmingMillis(maxDuration: true).double / FrameConstantsKt.FRAME_IN_MILLIS)
            let videoDuration = Int32(mediaView.getVideoDurationMs().double / FrameConstantsKt.FRAME_IN_MILLIS)
            let duration = min(videoDuration.int, maxVisibleDuration.int).int32 //todo this is ugly, fix it
            let startFrame = mediaView.getStartFrameShortCut()
            if ((startFrame...(startFrame + duration)).contains(frame)) {
                let timems = getOffsetMsForFrame(frame: frame.int)
                if (lastTime != timems) {
                    playerView.seekSyncTo(timeMs: timems) { [weak self] in
                        //todo
                    }
                }
                lastTime = timems
            }
        }
    }
    
    func getVideoDurationMs() -> Int64 {
        var duration = playerView.getDuration()
        guard let uri = mediaView!.media.originalSource else { return -1}
        if (duration == -1) {
            let localUrl: URL
            let sheme = uri.getSheme()
            if sheme.compare("assets:", options: .caseInsensitive) == .orderedSame {
                let asset = ResourceContainerExtKt.getAssetByFilePath(MR.assets(), filePath: uri.removeSheme())
                localUrl = asset.url
            }
            else {
                localUrl = URL(string: uri)!
            }
            duration = Int64(localUrl.videoDurationSeconds() * 1000)
        }
        return playerView.getDuration()
    }
    
    func interruptImageLoading() {
        
    }
    
    func addVideoProgressHandler(progressHandler: @escaping (CMTime) -> Void) {
        playerView.setProgressHandler(positionChangeHandler: progressHandler)
    }
    func removeProgressHandler() {
        playerView.removeProgressHandler()
    }
    
    func seekTo(timeMs: Int) {
        if (!isRecording) {
            playerView.seekTo(timeMs: timeMs)
        }
    }
    
    func isVideoHasAudio() -> Bool {
        return mediaView?.isVideo() == true && playerView.isAudioAvailable()
    }
    
    func isVideoPlayingState() -> Kotlinx_coroutines_coreStateFlow {
        return CoroutinesUtilKt.createStateFlow(value: false)
    }
    
    func videoCurrentTimeMs() -> Kotlinx_coroutines_coreStateFlow {
        return CoroutinesUtilKt.createStateFlow(value: Int64(0))
    }
    
    func loadNewImage(path: String, textureIndex: Int32, onSuccess: @escaping () -> Void) {
        setImageInitial(url: path, onError: { [weak self] _ in
            //todo
        }, onSuccess: onSuccess)
    }
    
    func pauseVideoIfExists() {
        if (mediaView?.isVideo() == true) {
            playerView.pause()
        }
    }
    
    private func startVideoPlaying() {
        let startTime = (mediaView?.media.videoStartTimeMs?.value as? KotlinInt)?.intValue ?? 0
        let volume: Float = ((mediaView?.media.videoVolume?.value as! KotlinFloat?)?.floatValue ?? 1.0)
        updateVideoVolume(volume: volume)
        playerView.seekTo(timeMs: startTime)
        playerView.play()
    }
    
    func playVideoIfExists(forcePlay: Bool) {
        
        if (mediaView?.isVideo() == true && !isRecording) {
            let isTemplatePlaying = (mediaView?.templateParent.isPlaying.value as? KotlinBoolean)?.boolValue == true
            if (canPlay(frame: mediaView?.currentFrame.int ?? 0) || (forcePlay && !isTemplatePlaying)) {
                startVideoPlaying()
            } else {
                waitToPlay = true
            }
        }
    }
    
    func refresh() {
        
    }
    
    func removeInnerMedia() {
        print("remove inner media")
    }
    
    func restartVideoIfExists() {
        guard mediaView?.isVideo() == true else { return }
        playerView.pause()
        playVideoIfExists(forcePlay: false)
    }
    
    func restoreRenderingInList() {
        
    }
    
    func setBlurRadius(blurRadius: Float, async: Bool) {
        self.blurRadius = blurRadius.cg
    }
    
    var original: UIImage? = nil
    
    func setColorFilter(color: KotlinInt?) {
        guard mediaView?.isVideo() != true else { return }
        
        if let color = color {
            if (original == nil) {
                original = imageView.image
                if (original == nil) { return } //todo
            }
            DispatchQueue.main.async { [weak self] in
                if let currentFilter = CIFilter(name: "CIMultiplyCompositing"),
                   let original = self?.original {
                    let beginImage = CIImage(image: original)
                    
                    let size = CGSize(width: original.size.width / UIScreen.main.scale, height: original.size.height / UIScreen.main.scale) //this is wrong, need to check this
                    let fc = UIImage(color: UIColor(cgColor: color.int32Value.ARGB.cgColor!), size: size)
                    let back = CIImage(image: fc!)
                    currentFilter.setValue(beginImage, forKey: kCIInputImageKey)
                    currentFilter.setValue(back, forKey: kCIInputBackgroundImageKey)
                    let context = CIContext(options: nil)
                    if let output = currentFilter.outputImage {
                        if let cgimg = context.createCGImage(output, from: output.extent) {
                            let processedImage = UIImage(cgImage: cgimg)
                            self?.imageView.image = processedImage
                        }
                    }
                }
            }
        } else {
            if let original = original {
                //DispatchQueue.main.async {
                self.imageView.image = original
                self.original = nil
                //}
            }
        }
    }
    
    private func updateImage(image: UIImage?) {
        imageView.image = image
    }
    
    func setDisplayVideo() -> Bool {
        return false
    }
    
    func onFrameSizeChanged(new: CGSize) {
        touchTransformHelper.onFrameSizeChanged(new: new)
        //initialVideoTransform()
        onMediaTransform()
    }
    
    private func onMediaTransform(fromUser: Bool = false) {
        guard mediaView?.view is ViewPlatformApple else { return }
        if (fromUser) {
            //may select this view on any gesture
            mediaView?.setSelected()
        }
        guard let media = mediaView?.media else { return }
        
        let transform: TransformMediaData
        
        if (mediaView?.isMediaDemo() == true) {
            //            let size = view.getCGSize()
            //            let mSize = touchTransformHelper.mediaSize
            //            let xscale = size.width / mSize.width
            //            let yscale = size.height / mSize.height
            
            transform = touchTransformHelper.getTransform(offsetX: media.demoOffsetX.cg, offsetY: media.demoOffsetY.cg, scale: media.demoScale.cg, rotate: media.innerImageRotation.cg, isDemo: true)
        } else {
            transform = touchTransformHelper.getTransform(offsetX: media.innerImageOffsetX.cg, offsetY: media.innerImageOffsetY.cg, scale: media.innerImageScale.cg, rotate: media.innerImageRotation.cg, isDemo: false)
        }
        let scale = CGAffineTransform(scaleX: transform.scale.cg, y: transform.scale.cg)
        let translate = CGAffineTransform(translationX: transform.translateX.cg, y: transform.translateY.cg)
        let rotate = CGAffineTransform(rotationAngle: Angle(degrees: transform.rotate.cg).radians)
        if (mediaView?.isVideo() == false) {
            imageView.transform = rotate.concatenating(scale).concatenating(translate)
            imageView.setNeedsDisplay()
        } else {
            playerView.innerPlayerView.transform = rotate.concatenating(scale).concatenating(translate)
            playerView.innerPlayerView.setNeedsDisplay()
        }
    }
    
    private func resetContainer(isVideo: Bool = false) {
        touchTransformHelper.setOnMediaTransformed{ [weak self] fromUser in
            self?.onMediaTransform(fromUser: fromUser)
        }
        /*
         * this condition avoids two bugs:
         * 1. incorrect gestures for editable images
         * 2. wrong size and position of image if it is a mask and it is in UIView container
         */
        
        if (mediaView?.userCanEdit() != true) {
            imageView.setContentCompressionResistancePriority(.fittingSizeLevel, for: .horizontal)
            imageView.setContentCompressionResistancePriority(.fittingSizeLevel, for: .vertical)
            if (isVideo) {
                playerView.innerPlayerView.setContentCompressionResistancePriority(.fittingSizeLevel, for: .horizontal)
                playerView.innerPlayerView.setContentCompressionResistancePriority(.fittingSizeLevel, for: .vertical)
            }
            
        } else
        {
            for view in mediaFrame.subviews{
                view.removeFromSuperview()
            }
            
            if (isVideo) {
                playerView.innerPlayerView.translatesAutoresizingMaskIntoConstraints = false
                mediaFrame.addSubview(playerView.innerPlayerView)
                NSLayoutConstraint.activate([
                    playerView.innerPlayerView.heightAnchor.constraint(equalTo: mediaFrame.heightAnchor),
                    playerView.innerPlayerView.widthAnchor.constraint(equalTo: mediaFrame.widthAnchor)
                ])
            } else {
                imageView.translatesAutoresizingMaskIntoConstraints = false
                mediaFrame.addSubview(imageView)
                NSLayoutConstraint.activate([
                    imageView.heightAnchor.constraint(equalTo: mediaFrame.heightAnchor),
                    imageView.widthAnchor.constraint(equalTo: mediaFrame.widthAnchor)
                ])
            }
        }
        imageView.image = nil
    }
    
    func setupTouchGesutures() {
        mediaFrame.gestureRecognizers?.removeAll()
        if (mediaView?.templateMode == TemplateMode.edit && mediaView?.media.isEditable == true && mediaView?.media.isMovable != true) {
            mediaFrame.setOnClickListener { [weak self] in
                self?.mediaView?.setSelected()
            }
            
            mediaFrame.setOnDragListener(action: touchTransformHelper.onDragAction)
            mediaFrame.setOnScaleListener(action: touchTransformHelper.onScaleAction)
            mediaFrame.setOnRotateListener(action: touchTransformHelper.onRotateAction)
        } else {
            if (mediaView?.media.isMovable == true) {
                mediaFrame.setOnClickListener { [weak self] in
                    self?.mediaView?.setSelected()
                }
                if let helper = mediaView?.movableTouchHelper as? MovableTouchHelperApple {
                    mediaFrame.setOnDragListener(isWrapper: false, action: helper.onDragEvent)
                }
            }
        }
    }
    
    private func prepareImage(image: UIImage, size: CGSize) {
        let msize = image.size
        
        if (touchTransformHelper.mediaSize == .zero) {
            touchTransformHelper.mediaSize = msize
        }
        initialMediaTransform(image: image)
        imageView.setNeedsDisplay()
        
        setUpMatrix()
        
        imageView.gestureRecognizers?.removeAll()
        
        setupTouchGesutures()
        
        mediaFrame.onFrameUpdated = { [weak self] bounds in
            self?.onFrameSizeChanged(new: bounds.size)
        }
        
        touchTransformHelper.reset()
        onMediaTransform()
        if (mediaView?.media.shape != nil) {
            mediaView?.setNewShape(shape: mediaView!.media.shape!, forDuplicated: false)
        }
        if let filter = mediaView?.media.colorFilter {
            setColorFilter(color: filter)
        }
        mediaView?.templateParent.objectWillChanged()
    }
    
    private func setInnerImage(url: URL, finishHandler: @escaping () -> ()) {
        
        let view = mediaView?.view as? ViewPlatformApple
        
        let size = view?.getCGSize() ?? CGSize(width: 100, height: 100)
        
        if (size.width == 0.cg) { return }
        DispatchQueue.main.async { [self] in
            resetContainer()
            
            
            let imageScale: CGFloat
            if (mediaView?.templateMode == .listDemo) {
                imageScale = UIScreen.main.scale  //1.cg
            } else {
                imageScale = UIScreen.main.scale
            }
            
            let resizingProcessor = ResizingImageProcessor(referenceSize: CGSize(width: size.width * imageScale, height: size.height * imageScale), mode: .aspectFill)
            KingfisherManager.shared.downloader.downloadTimeout = 60
            let resource = ImageResource(downloadURL: url, cacheKey: url.cacheKey)
            
            imageView.kf.setImage(
                with: resource,
                options: [.processor(resizingProcessor), .backgroundDecode, .cacheOriginalImage, .diskCacheExpiration(.never)],
                completionHandler: {
                    [weak self] result in
                    switch result {
                    case .failure(let error):
                        print("error retreiving image \(error)")
                    case .success(let result):
                        self?.prepareImage(image: result.image, size: size)
                        finishHandler()
                    }
                }
            )
        }
        
    }
    
    
    
    func setImageInitial(url: String?, onError: @escaping (KotlinThrowable?) -> Void, onSuccess: @escaping () -> Void) {
        doWhenSizeIsKnown { [weak self] in
            if let url = url {
                let sheme = url.getSheme()
                if sheme.compare("assets:", options: .caseInsensitive) == .orderedSame {
                    let asset = ResourceContainerExtKt.getAssetByFilePath(MR.assets(), filePath: url.removeSheme())
                    print("init image \(asset.url)")
                    self?.setInnerImage(url: asset.url) {
                        print("initialized image assset \(asset.url)")
                        onSuccess()
                    }
                }
                else {
                    print("init image \(url)")
                    self?.setInnerImage(url: URL(string: url)!) {
                        print("initialized image url \(url)")
                        onSuccess()
                    }
                }
            }
        }
    }
    
    func updateInnerOffset() {
        
    }
    
    func setInnerImageScale(scaleX: Float, scaleY: Float) {
        if let mediaView = mediaView {
            let view = (mediaView.view as! ViewPlatformApple)
            view.innerScaleX = scaleX.cg
            view.innerScaleY = scaleY.cg
        }
    }
    
    
    func setPickImage(onClick: (() -> Void)?) {
        
        setColorFilter(color: nil)
        let view = mediaView!.view as! ViewPlatformApple
        let size = view.getCGSize()
        resetContainer()
        
        let back = UIImage(color: UIColor(red: 0xe8.cg/0xff, green: 0xe8.cg/0xff, blue: 0xe8.cg/0xff, alpha: 1))!
        let icon = UIImage(named: "icon_add")! //42x53
        
        let iconSize = CGSize(width: 42.cg, height: 53.cg)
        if (size != .zero) {
            UIGraphicsBeginImageContextWithOptions(size, false, 0.0)
            let iconPoint = CGPoint(x: size.width/2 - iconSize.width/2, y: size.height/2 - iconSize.height/2)
            back.draw(in: CGRect(origin: .zero, size: size))
            icon.draw(in: CGRect(origin: iconPoint, size: iconSize))
            
            let newImage = UIGraphicsGetImageFromCurrentImageContext()
            UIGraphicsEndImageContext()
            
            touchTransformHelper.baseTransformData = nil
            view.innerRotation = 0
            view.innerScaleY = 1
            view.innerScaleX = 1
            view.innerTranslationY = 0
            view.innerTranslationX = 0
            //        onMediaTransform()
            
            updateImage(image: newImage)
            mediaFrame.gestureRecognizers?.removeAll()
            onMediaTransform()
        }
        
        if let onClick = onClick {
            imageView.setOnClickListener { [weak self] in
                print("tap \(self?.mediaView?.media.id)")
                if (self?.mediaView?.isSelectedForEdit == false) {
                    self?.mediaView?.setSelected()
                }
                onClick()
            }
        } else {
            imageView.gestureRecognizers?.removeAll()
        }
    }
    
    func setTranslateInner(translationX: Float, translationY: Float) {
        if let mediaView = mediaView {
            let view = (mediaView.view as! ViewPlatformApple)
            view.innerTranslationX = translationX.cg
            view.innerTranslationY = translationY.cg
        }
    }
    
    func setUpMatrix() {
        
    }
    
    private func preparePlayerView(localUrl: URL, gravity: AVLayerVideoGravity) {
        playerView.prepare(path: localUrl, videoGravity: gravity)
        
        if (mediaView?.getVideoEndTimeMs() ?? 0 < 0) {
            mediaView?.media.videoEndTimeMs?.setValue(mediaView!.getDurationForTrimmingMillis(maxDuration: true))
        }
        
        resetContainer(isVideo: true)
        mediaFrame.onFrameUpdated = { [weak self] bounds in
            self?.onFrameSizeChanged(new: bounds.size)
        }
        
        initialVideoTransform()
        onMediaTransform()
        
        
        
        setupTouchGesutures()
        if (mediaView?.media.shape != nil) {
            mediaView?.setNewShape(shape: mediaView!.media.shape!, forDuplicated: false)
        }
        if let start = mediaView?.media.videoStartTimeMs?.value {
            self.seekTo(timeMs: (start as! KotlinInt).intValue)
        }
        mediaView?.templateParentNullable?.childHasFinishedInitializing(inspView: mediaView!.asGeneric())
        mediaView?.templateParent.objectWillChanged()
    }
    
    func setVideoInner(uri: String, textureIndex: Int32) {
        
        doWhenSizeIsKnown { [weak self] in
            DispatchQueue.main.async {
                let localUrl: URL
                let sheme = uri.getSheme()
                let gravity: AVLayerVideoGravity
                if sheme.compare("assets:", options: .caseInsensitive) == .orderedSame {
                    let asset = ResourceContainerExtKt.getAssetByFilePath(MR.assets(), filePath: uri.removeSheme())
                    localUrl = asset.url
                    gravity = .resize
                }
                else {
                    localUrl = URL(string: uri)!
                    gravity = .resizeAspect
                }
                self?.preparePlayerView(localUrl: localUrl, gravity: gravity)
            }
        }
        
    }
    
    private func initialVideoTransform() {
        guard mediaView?.isVideo() == true else { return }
        let view = mediaView!.view as! ViewPlatformApple
        let size = view.getCGSize()
        touchTransformHelper.displaySize = size
        let videoSize = playerView.videoSize ?? size
        let videoAspect = videoSize.aspect()
        let frameAspect = size.aspect()
        let initialScale: CGFloat
        let realSize: CGSize
        
        //todo check it! may be wrong math
        if (frameAspect > videoAspect) {
            realSize = CGSize(width: size.height * videoAspect, height: size.height)
            initialScale = size.width / realSize.width
        } else {
            realSize = CGSize(width: size.width, height: size.width / videoAspect)
            initialScale = size.height / realSize.height
        }
        
        let trX = (realSize.width - realSize.width * initialScale) / (size.width * 2)
        let trY = (realSize.height - realSize.height * initialScale) / (size.height * 2)
        
        touchTransformHelper.updateBaseTransform(offsetX: trX, offsetY: trY, scale: initialScale)
        
        touchTransformHelper.mediaSize = realSize
    }
    
    private func initialMediaTransform(image: UIImage) {
        guard mediaView?.isVideo() != true else { return }
        let view = mediaView?.view as? ViewPlatformApple
        let size = view?.getCGSize() ?? .zero
        touchTransformHelper.displaySize = size
        let imageSize = image.size
        let realSize = imageSize.aspectFill(to: size)
        
        touchTransformHelper.mediaSize = realSize
    }
    
    func setVideoPositionIgnoreViewTiming() {
        
    }
    
    func setVideoTotalDurationMs(duration: Int32) {
        
    }
    
    func updateVideoCurrentTimeNoViewTimingMode() {
        
    }
    
    func updateVideoStartTime(originalUri: String, textureIndex: Int32, videoStartTimeMs: Int32) {
        
    }
    
    func updateVideoVolume(volume: Float){
        print("update video volume \(volume)")
        if (mediaView?.media.duplicate != nil) {
            playerView.player?.volume = 0
        } else {
            playerView.player?.volume = volume
        }
    }
    
    var framePreparedCallback: (() -> Void)? = nil
    
    
    func updateBorder() { //todo
        if let mediaView = self.mediaView {
            let view = mediaView.view as! ViewPlatformApple
            let c: Int32 = mediaView.media.borderColor?.int32Value ?? 0
            view.borderColor = c.ARGB
        }
        
    }
    
    deinit {
        playerView.release()
        //print("deinit inner media view! demo url = \(mediaView?.media.originalSource ?? mediaView?.media.demoSource)")
    }
}

extension InspMediaView {
    func userCanEdit() -> Bool {
        return media.isEditable || (media.isMovable?.boolValue ?? false)
    }
    
    func isMediaDemo() -> Bool {
        
        let isInListDemo = templateParent.templateMode == TemplateMode.listDemo && media.demoSource != nil && media.originalSource == nil
        
        return  isInListDemo // || isInListDemo
        
    }
    
    func getVideoStartTimeMs() -> Int {
        return (media.videoStartTimeMs?.value as? KotlinInt)?.intValue ?? 0
    }
    func getVideoEndTimeMs() -> Int {
        let endTime = (media.videoEndTimeMs?.value as? KotlinInt)?.intValue
        return endTime ?? Int(getVideoDurationMs())
    }
}

extension InspMediaView {
    func getUIImage() -> UIImage? {
        let innerMedia = innerMediaView as? InnerMediaViewApple
        return innerMedia?.imageView.image
    }
}


extension CGSize {
    func aspectFill(to size: CGSize) -> CGSize {
        let mW = size.width / self.width;
        let mH = size.height / self.height;
        
        var result = size
        if( mH > mW ) {
            result.width = size.height / self.height * self.width;
        }
        else if( mW > mH ) {
            result.height = size.width / self.width * self.height;
        }
        return result;
    }
}
