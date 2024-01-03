//
//  SelectedTrackContentView.swift
//  MusicFeatureIos
//
//  Created by vlad on 8/4/21.
//

import SwiftUI
import shared
import Kingfisher

struct SelectedTrackContentView: View {
    let colors: MusicColors
    let track: Track
    let album: Album
    let onClickPlayPause: () -> ()
    let tab: MusicTab
    let onNavigatioSubscribe: () -> ()
    
    @EnvironmentObject
    var musicPlayerViewModel: MusicPlayerViewModel
    
    @EnvironmentObject
    var musicDownloadingViewModel: MusicDownloadingViewModelApple
    
    @EnvironmentObject
    var licenseManagerWrapper: LicenseManagerAppleWrapper
    
    @State
    var sliderOwnValue: CGFloat = 0
    
    @State
    var isSliderInDrag: Bool = false
    
    
    var body: some View {
        HStack(spacing: 0) {
            Button(action: onClickPlayPause, label: {
                HStack(spacing: 0) {
                    TrackImageView(image: track.image, colors: colors,
                                   isSelected: true)
                    
                    VStack(spacing: 0) {
                        
                        HStack(spacing: 0) {
                            
                            Text(track.title)
                                .lineLimit(1)
                                .font(.system(size: 11, weight: .medium))
                                .foregroundColor(colors.trackTextTitle.toSColor())
                                .padding(.trailing, 6)
                            
                            if !track.artist.isUnknownArtist() {
                                Text(track.artist)
                                    .lineLimit(1)
                                    .font(.system(size: 9, weight: .regular))
                                    .foregroundColor(colors.trackTextSubtitle.toSColor())
                            }
                            
                            Spacer()
                        }
                        
                        ZStack(alignment: .center) {
                            
                            let bindingCurrentTime = Binding<CGFloat>(
                                get:{
                                    
                                    if isSliderInDrag {
                                        return sliderOwnValue
                                    } else {
                                        return musicPlayerViewModel.getProgress()
                                    }
                                },
                                
                                set: {newValue, _ in
                                    
                                    if isSliderInDrag {
                                        sliderOwnValue = newValue
                                    }
                                })
                            
                            
                            let thumbSize: CGSize = isSliderInDrag ? CGSize(width: 14, height: 14) : CGSize(width: 12, height: 12)
                        
                            ValueSlider(value: bindingCurrentTime, onEditingChanged: { isDragging in
                                
                                if !isDragging {
                                    musicPlayerViewModel.seekTo(progress: Double(sliderOwnValue))
                                    musicPlayerViewModel.currentTimeJob(assignTimeBeforeStart: false)
                                    isSliderInDrag = false
                                } else {
                                    musicPlayerViewModel.cancelCurrentTimeJob()
                                    isSliderInDrag = true
                                }
                                
                                
                            }).frame(maxHeight: .infinity, alignment: .center)
                                .valueSliderStyle(HorizontalValueSliderStyle(thumbSize: thumbSize))
                            
                            
                            /**MusicTrackSlider(colors: colors, value: isSliderInDrag ? $sliderOwnValue : bindingCurrentTime, onEditingChange: { isDragging in
                                
                                
                                isSliderInDrag = isDragging
                                
                                if !isDragging {
                                    musicPlayerViewModel.seekTo(progress: Double(sliderOwnValue))
                                }
                            })*/
                            
                        }.frame(height: 20, alignment: .center)
                        
                        
                        HStack(spacing: 0) {
                            
                            let currentTime = isSliderInDrag ? Int64(sliderOwnValue * CGFloat(musicPlayerViewModel.totalTime)) : musicPlayerViewModel.currentTime
                            
                            Text(TrackUtils.init().convertTimeToString(durationMs: currentTime))
                                .lineLimit(1)
                                .font(.system(size: 10, weight: .light))
                                .foregroundColor(colors.trackTextSubtitle.toSColor())
                            
                            Spacer()
                            
                            Text(TrackUtils.init().convertTimeToString(durationMs: musicPlayerViewModel.totalTime))
                                .lineLimit(1)
                                .font(.system(size: 10, weight: .light))
                                .foregroundColor(colors.trackTextSubtitle.toSColor())
                        }
                    }.padding(.leading, 10)
                    .padding(.trailing, 10)
                
                }
            })
            .frame(maxWidth: .infinity)
            
            
            Button(action: {
                
                if (musicDownloadingViewModel.coreModel.shouldOpenSubscribeOnPickMusic(tab: tab, hasPremium: licenseManagerWrapper.hasPremium)) {
                    
                    onNavigatioSubscribe()
                } else {
                    
                    musicDownloadingViewModel.coreModel.pickMusic(item: track, album: album, durationMillis: musicPlayerViewModel.totalTime, tab: tab)
                }
                
            }, label: {
                ZStack {
                    Image("ic_music_add")
                }
                .frame(width: 26, height: 26, alignment: .center)
                
            }).frame(width: 40, height: 40, alignment: .center)
                .padding(.trailing, 20)
        }
        
    }
}

struct SelectedTrackContentView_Previews: PreviewProvider {
    static var previews: some View {
        let track = getTestTracks()[0]
        
        SelectedTrackContentView(colors: MusicDarkColors(), track: track, album: getTestAlbums()[0], onClickPlayPause: {}, tab: MusicTab.library, onNavigatioSubscribe: {})
            .environmentObject(MusicPlayerViewModel(isPlaying: true, selectedTrack: track.url))
            .environmentObject(MusicDownloadingViewModelApple(Dependencies.resolveAuto()))
    }
}
