//
//  TrackImageView.swift
//  MusicFeatureIos
//
//  Created by vlad on 11/4/21.
//

import SwiftUI
import Kingfisher
import shared

struct TrackImageView: View {
    let image: String?
    let colors: MusicColors
    let isSelected: Bool
    
    @EnvironmentObject
    var musicPlayerViewModel: MusicPlayerViewModel
    
    var body: some View {
        
        GeometryReader { geo in
            let requiredImageSize = Int(geo.size.width)
            
            ZStack {
                
                if (image != nil) {
                    
                    let imageUrl = image!.replaceImageSizeItunes(size: requiredImageSize)
                    
                    KFImage(URL(string: imageUrl))
                        .placeholder {
                            if (!isSelected) {
                                ZStack {
                                    
                                    SVGImage(svgName: "ic_music_track_placeholder")
                                        .frame(width: 40, height: 40, alignment: .center)
                                        .background(colors.trackPlayPauseBgInactive.toSColor())
                                    
                                    
                                }.frame(width: 40, height: 40)
                            } else {
                                ZStack {
                                    
                                }.frame(width: 40, height: 40, alignment: .center)
                                .background(colors.trackPlayPauseBgActivePlaceholder.toSColor())
                            }
                            
                        }
                        .downsampling(size: CGSize(width: requiredImageSize, height: requiredImageSize))
                        .cancelOnDisappear(true)
                        .resizable()
                        .frame(width: 40, height: 40)
                        .cornerRadius(8)
//                        .overlay(
//                            RoundedRectangle(cornerRadius: 8)
//                                .stroke(isSelected ? colors.trackSelectedImageBorder.toSColor() : .clear, lineWidth: 4)
//                        )
                    
                    
                } else {
                    SVGImage(svgName: "ic_music_track_placeholder")
                        .frame(width: 40, height: 40, alignment: .center)
                        .background(colors.trackPlayPauseBgInactive.toSColor())
                        .cornerRadius(8)
                }
                
                if (isSelected) {
                    if (musicPlayerViewModel.isLoading) {
                        
                        ZStack {
                            ProgressView()
                            
                        }.frame(width: 40, height: 40, alignment: .center)
                        .background(colors.trackPlayPauseBgActive.toSColor())
                        .cornerRadius(8)
                        
                    } else {
                        SVGImage(svgName: musicPlayerViewModel.isPlaying ? "ic_music_pause_placeholder" : "ic_music_play_placeholder")
                            .frame(width: 40, height: 40, alignment: .center)
                            .background(colors.trackPlayPauseBgActive.toSColor())
                            .cornerRadius(8)
                    }
                    
                }
                
            }.frame(width: 40, height: 40)
            
        }
        .frame(width: 40, height: 40).padding(.leading, 18)
    }
}

struct TrackImageView_Previews: PreviewProvider {
    static var previews: some View {
        let testTrack = getTestTracks()[0]
        TrackImageView(image: testTrack.image, colors: MusicDarkColors(), isSelected: true)
            .environmentObject(MusicPlayerViewModel(isPlaying: true, selectedTrack: testTrack.url))
    }
}
