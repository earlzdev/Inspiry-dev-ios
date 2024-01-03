//
//  ExportViewModel.swift
//  iosApp
//
//  Created by vlad on 8/11/21.
//

import Foundation
import SwiftUI
import UIKit
import AVFoundation
import shared

class ExportViewModel: ObservableObject {
    
    @Published
    public private(set) var state: ExportState = ExportState.Initial(imageElseVideo: false)
    
    @Published
    private(set) var preview: UIImage?
    
    @Published
    private(set) var videoPreview: URL? = nil
    
    @Published var aspect: Float = 1
    
    @Published var notAuthorizedMessage: Bool = false
    
    //********************************************
    //  debug features
    
    @Published var debugRenderTimeCurrent: Double = 0 {
        didSet {
            debugRenderTimeFull += debugRenderTimeCurrent
        }
    }
    @Published var debugRenderTimeFull: Double = 0
    
    //*******************************************
    
    private var templateSize: CGSize = .zero
    
    private var templateView: InspTemplateView?
    
    private var analyticsManager: AnalyticsManager = Dependencies.resolveAuto()
    
    init(editableTemplate: InspTemplateView) {
        self.templateView = nil
        self.preview = nil
        
        DispatchQueue.global().async {
            let template = editableTemplate.getCopyOfTemplate()
            DispatchQueue.main.async { [weak self] in
                self?.aspect = template.format.aspectRatio()
                self?.templateSize = template.format.getRenderingSize().cg
                self?.templateView = InspTemplateViewApple (templatePath: (editableTemplate as! InspTemplateViewApple).templatePath, template: template)
                self?.templateView?.doWhenTemplateInitialized {[weak self] in
                    self?.templateView?.setFrameForEdit()
                    self?.initialPreview()
                }
                self?.initTemplate(template: template)
            }
            
        }
    }
    
    private func initTemplate(template: Template) {
        guard let templateView = templateView else {
            return
        }
        
        templateView.recordMode = .image
        templateView.templateMode = .preview
        DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) { [weak self] in
            (templateView as! InspTemplateViewApple).templateVisible = true
            templateView.loadTemplate(template: template)
            templateView.recordMode = .image
            (templateView as! InspTemplateViewApple).onTemplateSizeChanged(newSize: self?.templateSize ?? .zero)
            
        }
        
    }
    
    private func getFileNameForExport() -> String {
        let templateName = templateView?.template_.getNameForShare()
        let dateFormatter = DateFormatter()
        dateFormatter.dateFormat = "hh_mm_ss_dd_MM_YY"
        let dateString = dateFormatter.string(from: Date.init(timeIntervalSinceNow: 0))
        return "\(templateName ?? "Inspiry")_\(dateString)"
    }
    
    private func initialPreview() {
        DispatchQueue.main .async(qos: .userInteractive) { [weak self] in
            let renderer = ViewToImageRenderer(size: self?.templateSize ?? .zero) {
                InnerTemplateView()
                    .animation(nil)
                    .environmentObject(self?.templateView as! InspTemplateViewApple)
            }
            self?.templateView?.setVideoFrameSync(frame: 0, sequential: false)
            self?.templateView?.objectWillChanged()
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) { [weak self] in
                self?.preview = renderer.renderImage()
            }

        }
    }
    
    func onChangeImageElseVideo(imageElseVideo: Bool) {
        state = ExportState.Initial(imageElseVideo: imageElseVideo)
    }
    
    func userPickedWhereToExport(whereToExport: WhereToExport, fromDialog: Bool) {
        guard let templateView = templateView else {
            return
        }
        
        if state is ExportState.Initial {
            state = ExportState.UserPicked(imageElseVideo: state.imageElseVideo,
                                                whereToExport: whereToExport, fromDialog: fromDialog)
        } else {
            if let state = state as? ExportState.Rendered {
                self.state = ExportState.Rendered(imageElseVideo: self.state.imageElseVideo, whereToExport: whereToExport, fromDialog: state.fromDialog, file: state.file, localId: state.localId)
            }
        }
        
        (templateView as! InspTemplateViewApple).templateVisible = true
        
        if (state.imageElseVideo) {
            templateView.recordMode = .image
            debugRenderTimeFull = 0
            renderImageToFile()
            
        } else {
            if (!(state is ExportState.Rendered)) {
                templateView.recordMode = .video
                templateView.refreshAllMedias()
                debugRenderTimeFull = 0
                recordVideo() { [weak self] in
                    self?.shareTo(whereToExport: whereToExport, fromDialog: fromDialog)
                }
            } else {
                shareTo(whereToExport: whereToExport, fromDialog: fromDialog)
            }
        }
    }
    
    private func shareTo (whereToExport: WhereToExport, fromDialog: Bool) {
        print("where to export = \(whereToExport.whereApp)")
        if let state = state as? ExportState.Rendered {
            switch(state.whereToExport?.whereApp) {
            case "instagram": shareToInstagram()
            case "Gallery": break
            default: shareToMore()
                
            }
            sendAnalytics(whereToExport: whereToExport, fromDialog: fromDialog)
        }
    }
    
    func sendAnalytics(whereToExport: WhereToExport, fromDialog: Bool) {
        guard let template = templateView?.template_ else { return }
        analyticsManager.shareTemplate(
            activityName: whereToExport.whereScreen,
            fromDialog: fromDialog,
            animatedElseStatic: state.imageElseVideo,
            template: template
        )
    }
    
    func shareToInstagram() {
        guard let state = state as? ExportState.Rendered else { return }
        let url = URL(string: "instagram://library?LocalIdentifier=\(state.localId!)")!
        DispatchQueue.main.async {
            if UIApplication.shared.canOpenURL(url) {
                UIApplication.shared.open(url)
            } else {
                let alertController = UIAlertController(title: "Error", message: "Instagram is not installed", preferredStyle: .alert)
                alertController.addAction(UIAlertAction(title: "OK", style: .default, handler: nil))
                getCurrentWindow()?.rootViewController?.present(alertController, animated: true, completion: nil)
            }
        }
    }
    
    func shareToMore() {
        guard let state = state as? ExportState.Rendered else {
            print("current state is not rendered")
            return }
        //guard let imageData = preview!.pngData() else { debugPrint("Cannot convert image to data!"); return }
        DispatchQueue.main.async { [weak self] in
            let ctrl = getCurrentWindow()?.rootViewController//?.view
            let tempFile: URL
            if (state.imageElseVideo) {
                tempFile = RenderSettings.getTempUrl(filename: self?.getFileNameForExport() ?? "tempfilename", fileExtension: "jpg", clear: true)
                do {
                    try self?.preview?.pngData()?.write(to: tempFile, options: .atomic)
                } catch {
                    
                }
            } else {
                tempFile = URL(string: state.file)!
            }
            
            let activityController = UIActivityViewController(activityItems: [tempFile], applicationActivities: nil)
            
            ctrl?.present(activityController, animated: true) {
                
            }
        }
    }
    
    private func renderImageToFile() {
        guard let preview = preview else {
            return
        }
        let filename = getFileNameForExport()
        CustomPhotoAlbum.sharedInstance.save(preview, filename: filename) { [weak self] localId, url in
            if let s = self {
                s.state = ExportState.Rendered(imageElseVideo: s.state.imageElseVideo, whereToExport: s.state.whereToExport!, fromDialog: s.state.fromDialog, file: url?.absoluteString ?? "", localId: localId ?? "") //todo
                if (url != nil) {
                    s.shareTo(whereToExport: s.state.whereToExport!, fromDialog: false)
                } else {
                    s.notAuthorizedMessage = true
                }
            }
        }
    }
    
    func openGalleryApp() {
        UIApplication.shared.open(URL(string:"photos-redirect://")!)
    }
    
    
    private func progressUpdate(progress: Float) {
        self.state = ExportState.RenderingInProcess(imageElseVideo: self.state.imageElseVideo, whereToExport: self.state.whereToExport!, fromDialog: self.state.fromDialog, progress: KotlinFloat(value: progress))
    }
    
    private var isCanceled = false
    
    func onClickBack() {
        isCanceled = true
        templateView?.stopPlaying()
        templateView?.unloadTemplate()
        templateView = nil
    }
    
    
    private func recordFinished(url: URL, localId: String) {
        
        DispatchQueue.main.async { [weak self] in
            if let self = self {
                self.videoPreview = url
                //}
                self.state = ExportState.Rendered(imageElseVideo: self.state.imageElseVideo, whereToExport: self.state.whereToExport!, fromDialog:self.state.fromDialog, file: url.absoluteString, localId: localId)
                if (localId.isEmpty) {
                    self.notAuthorizedMessage = true
                }
            }
        }
    }
    
    let renderQueue = DispatchQueue(label: "renderQueue", qos: .default)
    let mediaQueue = DispatchQueue(label: "mediaInputQueue", qos: .default)
    
    private func updateTemplateFrame(frame: Int) {
        templateView?.prepareAnimation(frame: frame.int32)
        templateView?.setVideoFrameSync(frame: frame.int32, sequential: false)
        templateView?.setFrameSync(frame: frame.int32)
        templateView?.objectWillChanged()
    }
    
    private func recordVideo(complectionHandler: @escaping () -> Void) {
        guard let templateView = templateView else {
            return
        }
        
        let fileName = getFileNameForExport()
        
        let duration = templateView.getDuration_().int
        let renderer = ViewToImageRenderer(size: self.templateSize) {
            InnerTemplateView()
                .animation(nil)
                .environmentObject(templateView as! InspTemplateViewApple)
        }
        (templateView as! InspTemplateViewApple).onTemplateSizeChanged(newSize: self.templateSize)
        let renderSettings = RenderSettings(size: self.templateSize, fps: 30, avCodecKey: .hevc, videoFilenameExt: "mp4", tempName: fileName)
        let videoRenderer = VideoWriter(renderSettings: renderSettings)
        let frameDuration = CMTimeMake(value: Int64(600 / renderSettings.fps), timescale: 600)
        renderSettings.clearTempFiles()
        videoRenderer.start()
        
        self.preview = renderer.uiView.drawToImage()
        
        renderQueue.async { [weak self] in
            func writeMixed() {
                if let audioSources = templateView.getOriginalAudioDataForRecordAllTracks() {
                    audioSources.mergeTo(videoURL: renderSettings.tempURL) { composition, audioMix in
                        let exporter = AVAssetExportSession(asset: composition, presetName: AVAssetExportPresetHEVCHighestQuality)
                        exporter?.audioMix = audioMix
                        
                        
                        exporter!.outputURL = renderSettings.tempMixedURL
                        exporter!.outputFileType = .mp4
                        exporter!.timeRange = CMTimeRange(start: CMTime.zero, duration: CMTimeMake(value: Int64(templateView.getDuration_() * 600 / renderSettings.fps), timescale: 600))
                        exporter!.exportAsynchronously {
                            
                            switch exporter!.status {
                            case .failed:
                                print("Export failed: \(exporter!.error!)")
                            case .cancelled:
                                print("Export canceled")
                            default:
                                CustomPhotoAlbum.sharedInstance.saveToLibrary(videoURL: renderSettings.tempMixedURL) { localId, url in
                                    self?.recordFinished(url: exporter!.outputURL!, localId: localId ?? "")
                                    print("Successfully saved video with audio \(exporter!.outputURL!)")
                                    complectionHandler()
                                }
                                
                            }
                        }
                    }
                } else {
                    CustomPhotoAlbum.sharedInstance.saveToLibrary(videoURL: renderSettings.tempURL) { localId, url in
                        self?.recordFinished(url: renderSettings.tempURL, localId: localId ?? "")
                        print("Successfully saved video without audio \(renderSettings.tempURL)")
                        complectionHandler()
                    }
                }
            }
            
            let group = DispatchGroup()
            var frame = 0
                if videoRenderer.isReadyForData == false {
                    fatalError("video renderer full buffer!")
                }
            guard let self = self else { return }
            videoRenderer.videoWriterInput.requestMediaDataWhenReady(on: self.mediaQueue) {
                    if (self.isCanceled) {
                        videoRenderer.videoWriter.cancelWriting()
                    } else {
                        if (frame <= duration) {

                            let start = CFAbsoluteTimeGetCurrent()
                            let presentationTime = CMTimeMultiply(frameDuration, multiplier: frame.int32)


                            group.enter()

                            DispatchQueue.main.async {
                                print("record update frame \(frame)")
                                self.updateTemplateFrame(frame: frame)
                                let img = renderer.uiView.drawToImage()
                                self.preview = img
                                DispatchQueue.main.asyncAfter(deadline: .now() + 0.01) { //this is wrong but I dont have any idea how to fix skip frames bug yet
                                    videoRenderer.addImage(image: img, withPresentationTime: presentationTime)
                                    group.leave()
                                }
                                print("record added frame \(frame)")
                            }
                            group.wait()
                            
                            DispatchQueue.main.async {
                                let diff = CFAbsoluteTimeGetCurrent() - start
                                    self.debugRenderTimeCurrent = diff
                                    self.progressUpdate(progress: frame.cg.float/duration.cg.float)
                            }

                            frame += 1
                            
                            
                        } else {
                            videoRenderer.videoWriterInput.markAsFinished()
                            videoRenderer.videoWriter.finishWriting() {
                                writeMixed()
                            }
                        }
                    }
                }
//            }
            
        }
    }
}

extension Size {
    var cg: CGSize { return CGSize(width: width.cg, height: height.cg) }
}
