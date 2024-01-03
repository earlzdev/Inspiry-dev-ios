//
//  MusicEditModelApple.swift
//  iosApp
//
//  Created by rst10h on 17.03.22.
//

import SwiftUI
import shared
import DSWaveformImage
import Combine

class MusicEditModelApple: ObservableObject {
    
    static let SAMPLES_DISPLAY_COUNT = 72
    
    @Published
    var musicPlayerModel: MusicPlayerViewModel
    
    @Published
    var volume: Float = 1 {
        didSet {
            music.volume = Int32(volume * 100)
            templateView?.musicPlayer?.setVolume(volume: volume)
        }
    }
    
    private let templateView: InspTemplateView?
    
    @Published
    var startPlayPositionMs: Int64 = 0
    
    @Published
    var initialPosition: Int64 = 0
    
    var music: TemplateMusic
    private var sampleSize: Float = 0
    
    let templateDuration: Int //in ms
    
    let sample_time_ms: Int
    
    let scrollDetector: CurrentValueSubject<Int64, Never> = CurrentValueSubject<Int64, Never>(0)
    
    let samplesValues: CurrentValueSubject<[Float], Never> = CurrentValueSubject<[Float], Never>([Float]())
    
    let libraryCallback: () -> Void
    
    let analyticsManager: AnalyticsManager = Dependencies.resolveAuto()
    
    let initialStartTime: Int64
    let initialVolume: Float
    
    init(musicPlayerModel: MusicPlayerViewModel, templateView: InspTemplateView?, onLibrarySelected: @escaping () -> Void) {
        self.musicPlayerModel = musicPlayerModel
        self.templateView = templateView
        self.libraryCallback = onLibrarySelected
        self.music = templateView?.template_.music ?? TemplateMusic(
            url: "preview",
            title: "preview",
            artist: "preview",
            album: "preview",
            duration: Int64((templateView?.getDuration_() ?? 240).double * FrameConstantsKt.FRAME_IN_MILLIS * 1000),
            trimStartTime: 0,
            volume:0,
            tab: MusicTab.library,
            albumId: 0
        )

        musicPlayerModel.prepare(url: music.url, startPlayImmediately: false, position: music.trimStartTime.double) //position in miliseconds
        
        
        self.templateDuration = (templateView?.getDuration_().int ?? 8000) * 1000 / FrameConstantsKt.FPS.int
        self.sample_time_ms = min(self.templateDuration, music.duration.int) / Self.SAMPLES_DISPLAY_COUNT
        let count = music.duration / Int64(self.sample_time_ms)

        self.volume = self.music.volume.float / 100.0
        self.initialVolume = music.volume.float / 100.0
        self.initialStartTime = music.trimStartTime
        musicPlayerModel.setVolume(volume: self.volume)
        self.startPlayPositionMs = music.trimStartTime
        if let url = URL(string: music.url) {
            let waveformAnalyzer = WaveformAnalyzer(audioAssetURL: url)
            
            waveformAnalyzer?.samples(count: Int(count)) { [weak self] samplesArray in
                DispatchQueue.main.async {
                    if (samplesArray != nil && samplesArray?.isEmpty != true) {
                        self?.samplesValues.send(samplesArray!)
                    }
                }
            }
        }

    }
    var isInitialScroll = true
    func updatePlayOffset(x: Int64) {
        guard !isInitialScroll else {
            isInitialScroll = false
            return
            
        }
        if (x != startPlayPositionMs) {
            onScrollStarted()
            scrollDetector.send(x)
        }
        startPlayPositionMs = x
    }
    
    func onPlayPositionChanged(positionMs: Int64) {
        if ((positionMs - music.trimStartTime) > templateDuration) { musicPlayerModel.seekToPositionMs(timeMs: music.trimStartTime) }
    }
    
    func musicOffset() {
        music.trimStartTime = startPlayPositionMs
        musicPlayerModel.seekToPositionMs(timeMs: startPlayPositionMs)
    }
    
    private var playAfterScroll: Bool = false
    private var dropScrollValue: Bool = true
    func onScrollStarted() {
        if (playAfterScroll || musicPlayerModel.isPlaying == false) { return }
        playAfterScroll = true
        stopTemplate()
    }
    
    func onScrollEnded() {
        if (musicPlayerModel.isPlaying) {
            musicOffset()
            return
        }
        if (!playAfterScroll) { return }
        musicOffset()
        playAfterScroll = false
        playTemplate()
    }
    
    private func playTemplate() {
        guard templateView?.templateMode != .preview else { return }
        startPlayPositionMs = music.trimStartTime
        templateView?.currentFrame = 0
        (templateView as? InspTemplateViewApple)?.isMusicEnabled = false
        musicPlayerModel.play()
        templateView?.textViewsAlwaysVisible = false
        templateView?.startPlaying(resetFrame: true, mayPlayMusic: false)
    }
    
    func sendAnalytics() {
        if let music = templateView?.template_.music {
            analyticsManager.onMusicEditDialogClose(music: music, initialStartTime: self.initialStartTime, initialVolume: Int32(initialVolume * 100))
        }
    }
    
    func stopTemplate() {
        print("stop template")
        guard templateView?.templateMode != .preview else { return }
            musicPlayerModel.pause()
            templateView?.stopPlaying()
            templateView?.textViewsAlwaysVisible = true
            templateView?.setFrameForEdit()
    }
    
    func musicPlayPause() {

        if (musicPlayerModel.isPlaying) {
            stopTemplate()
            
        } else {
            musicOffset()
            playTemplate()
            
        }
        self.objectWillChange.send()
        
    }
    
    func sampleWidth(fullWidth: CGFloat, padding: CGFloat) -> CGFloat {
        let waveWidth = fullWidth - padding * 2 - (MusicEditModelApple.SAMPLES_DISPLAY_COUNT).cg
        let result = waveWidth / (MusicEditModelApple.SAMPLES_DISPLAY_COUNT + 1).cg
        return result
    }
    
    func getSampleColor(sampleIndex: Int, startPositionMS: Int64, currentPositionMS: Int64) -> SwiftUI.Color {
        
        let currentTime = sampleIndex * sample_time_ms
        if (startPositionMS < currentPositionMS && currentTime >= startPositionMS && currentTime < currentPositionMS - 1) {
            return 0xFFFF76E9.ARGB
        }
        
        if (startPositionMS >= currentPositionMS && currentTime <= startPositionMS && currentTime >= currentPositionMS) {
            return 0xFFFF76E9.ARGB
        }
        if (currentTime < startPositionMS || currentTime > startPositionMS + templateDuration.int64) { return 0xFF828282.ARGB }
        return Color.white
    }
}
