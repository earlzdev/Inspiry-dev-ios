//
//  MusicPanelUI.swift
//  iosApp
//
//  Created by rst10h on 16.03.22.
//

import SwiftUI
import shared
import Combine

struct MusicPanelUI: View {
    @ObservedObject
    var model: MusicEditModelApple
    @ObservedObject
    var playerModel: MusicPlayerViewModel
    
    @State
    private var samples: [Float] = [Float]()
    
    private let scrollStatePublisher: AnyPublisher<Int64, Never>
    private let samplesPublisher: AnyPublisher<[Float], Never>
    
    init(model: MusicEditModelApple) {
        _model = ObservedObject(wrappedValue: model)
        _playerModel = ObservedObject(wrappedValue: model.musicPlayerModel)
        self.scrollStatePublisher = model.scrollDetector
            .debounce(for: .seconds(0.2), scheduler: DispatchQueue.main)
            .dropFirst()
            .eraseToAnyPublisher()
        self.samplesPublisher = model.samplesValues
            .eraseToAnyPublisher()
    }
    
    init(model: MusicEditModelApple, samples: [Float]) {
        self.init(model: model)
        self.samples = samples
    }
    
    var body: some View {
        VStack(spacing: 0.cg) {
            ZStack {
                Text(TrackUtils.init().convertTimeToString(durationMs: model.startPlayPositionMs))
                    .foregroundColor(0xffee7bdc.ARGB)
                    .font(.system(size: 12.cg))
                    .padding(.leading, 50.cg)
                    .frame(
                        maxWidth: .infinity,
                        alignment: .leading)
                
                Text(playerModel.isPlaying ? TrackUtils.init().convertTimeToString(durationMs: abs(playerModel.currentTime - model.startPlayPositionMs)) : "00:00")
                    .foregroundColor(0xffd0d0d0.ARGB)
                    .font(.system(size: 12.cg))
                    .padding(.trailing, 20.cg)
                    .frame(
                        maxWidth: .infinity,
                        alignment: .trailing)
            }
            .padding(.bottom, 5.cg)
            HStack(spacing: 0) {
                PlayPauseIcon(isPlaying: $playerModel.isPlaying)
                    .onTapGesture {
                        model.musicPlayPause()
                    }
                GeometryReader { geo in
                    let sampleWidth = model.sampleWidth(fullWidth: geo.size.width, padding: 35.cg)
                    ZStack {
                        if (samples.count > 0) {
                            OffsetableScrollView(.horizontal) {
                                ScrollViewReader { sv in
                                    HStack(alignment: .center, spacing: 1) {
                                        ForEach(samples.indices, id: \.self) { index in
                                            
                                            let sColor = model.getSampleColor(sampleIndex: index, startPositionMS: model.startPlayPositionMs, currentPositionMS: playerModel.currentTime)
                                            RoundedRectangle(cornerRadius: 2)
                                                .fill(sColor)
                                                .frame(width: sampleWidth, height: (samples[index] * 42).cg)
                                                .animation(.none)
                                            
                                        }
                                        Spacer(minLength: 0)
                                    }
                                    .padding(.horizontal, 35.cg)
                                    .frame(minWidth: 0, maxWidth: .infinity, minHeight: 42.cg, maxHeight: 42.cg)
                                    .background(0xff4f4f4f.ARGB)
                                    .drawingGroup()
                                    .onAppear {
                                        let newPos = Int(model.initialPosition) / model.sample_time_ms
                                        if (newPos > 0) {
                                            DispatchQueue.main.asyncAfter(deadline: .now() + 0.2) {
                                                 model.isInitialScroll = true
                                                sv.scrollTo(MusicEditModelApple.SAMPLES_DISPLAY_COUNT + newPos)
                                            }
                                        }
                                    
                                    }
                                    
                                }
                            }
                            .onScroll { x in
                                let newPosition = (x / (sampleWidth + 1) * model.sample_time_ms.cg).toInt.int64
                                model.updatePlayOffset(x: newPosition)
                                
                            }

                            .frame(maxHeight: 42.cg)
                            HStack(spacing: 0) {
                                Rectangle()
                                    .fill(0xffee7bdc.ARGB)
                                    .frame(width: 2.cg, height: 42.cg)
                                Spacer()
                                Rectangle()
                                    .fill(0xffee7bdc.ARGB)
                                    .frame(width: 2.cg, height: 42.cg)
                            }
                            .padding(.horizontal, 35.cg)
                        }
                    }
                }
                .frame(height: 42.cg)
                .onReceive(samplesPublisher) {
                    samples = $0
                    if (samples.count > 0) {
                        model.initialPosition = model.music.trimStartTime
                    }
                }
                
            }
            .cornerRadius(7)
            .padding(.bottom)
            
            HStack {
                VolumeSliderWithIcons(volume: $model.volume, onLibraryClick: model.libraryCallback)
                    //.environmentObject(model)
            }
            .frame(minWidth: 0, maxWidth: .infinity, maxHeight: 32.cg)
            
        }
        .frame(
            minWidth: 0,
            maxWidth: .infinity,
            alignment: .topLeading
        )
        .padding(.horizontal, 28.cg)
        .padding(.vertical)
        .background(0xff292929.ARGB)
        .onReceive(scrollStatePublisher) { _ in
            model.onScrollEnded()
        }
        .onChange(of: playerModel.currentTime) {
            model.onPlayPositionChanged(positionMs: $0)
        }
        .onChange(of: model.volume) {
            playerModel.setVolume(volume: $0)
        }
        .onDisappear {
            model.stopTemplate()
            model.sendAnalytics()
        }
    }
}

struct MusicPanelUI_Previews: PreviewProvider {
    static var samples: [Float] = (0...200).map { _ in Float(.random(in: 0...6)/10.0)}
    static var previews: some View {
        MusicPanelUI(model: MusicEditModelApple(musicPlayerModel: MusicPlayerViewModel(), templateView: nil) {}, samples: samples)
    }
}
