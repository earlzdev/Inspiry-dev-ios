//
//  MusicTabContentView.swift
//  MusicFeatureIos
//
//  Created by vlad on 8/4/21.
//

import SwiftUI
import shared

struct MusicTabContentView: View {
    let colors: MusicColors
    let contentTracks: InspResponse<TracksResponse>
    let contentAlbums: InspResponse<AlbumsResponse>
    
    let selectedAlbumId: Int64
    let onSelectedAlbumIdChange: (Int64) -> ()
    let onRetryTracksClick: () -> ()
    let onRetryAlbumsClick: () -> ()
    
    let showTracksSearchBar: Bool
    let usageWarning: AnyView?
    let tab: MusicTab
    let onNavigationSubscribe: () -> ()
    
    @EnvironmentObject
    var musicPlayerViewModel: MusicPlayerViewModel
    
    var body: some View {
        
        if usageWarning != nil {
            usageWarning!
        }
        
        ZStack(alignment: .leading) {
            
            if (contentAlbums is InspResponseLoading<AlbumsResponse>) {
                
                ProgressView().frame(maxWidth: .infinity, alignment: .center)
                
            } else if (contentAlbums is InspResponseError<AlbumsResponse>) {
                
                let errorMessage = (contentAlbums as! InspResponseError<AlbumsResponse>).throwable.message
                MusicErrorView(colors: colors, errorMessage: errorMessage, onRetryClick: onRetryAlbumsClick).frame(maxWidth: .infinity, alignment: .center)
                
            } else {
                let actualAlbums = (contentAlbums as! InspResponseData<AlbumsResponse>).data!.albums
                
                AlbumsListView(colors: colors, albums: actualAlbums, selectedAlbumId: selectedAlbumId,
                               onSelectedAlbumIdChange: onSelectedAlbumIdChange)
            }
            
            
        }
        .frame(height: 105, alignment: .leading)
        .frame(maxWidth: .infinity, minHeight: 105, maxHeight: 105)
        
        
        if (contentTracks is InspResponseLoading<TracksResponse>) {
            
            ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
            
        } else if (contentTracks is InspResponseError<TracksResponse>) {
            
            let error = (contentTracks as! InspResponseError<TracksResponse>).throwable
            
            let _ = error.printStackTrace()
            
            let errorMessage = error.message
            MusicErrorView(colors: colors, errorMessage: errorMessage, onRetryClick: onRetryTracksClick).frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
            
            
        } else {
            let actualTracks = (contentTracks as! InspResponseData<TracksResponse>).data
            
            TracksListView(colors: colors, tracks: actualTracks!.tracks, album: actualTracks!.album, showSearchBar: showTracksSearchBar, tab: tab, onNavigatioSubscribe: onNavigationSubscribe)
                .environmentObject(musicPlayerViewModel).frame(maxHeight: .infinity, alignment: .top)
            
        }
        
    }
}

func warningItunes(colors: MusicColors) -> some View {
    
    return Text(MR.strings.init().music_preview_warning.localized())
        .multilineTextAlignment(.center)
        .font(.system(size: 12, weight: .light))
        .foregroundColor(colors.headerPreviewText.toSColor())
        .frame(maxWidth: .infinity, alignment: .center)
        .padding(.horizontal, 15)
        .padding(.bottom, 4)
    
}

func royaltyFreeMusicHeader(colors: MusicColors, hasPremium: Bool, viewModel: MusicDownloadingViewModel) -> some View {
    
    
    return VStack {
        
        Text(MR.strings().music_royalty_free_description.localized())
            .multilineTextAlignment(.center)
            .foregroundColor(colors.searchTextInactive.toSColor())
            .font(.system(size: 13, weight: .light))
            .frame(maxWidth: .infinity)
            .padding(.horizontal, 50)
            .padding(.bottom, 7)
        
        if !hasPremium {
            VStack {
                
                if #available(iOS 15.0, *) {
                    Text(viewModel.getTextRoyaltyFreeTracksLeft(resToString: { res in
                        res.localized()
                    }))
                        .padding(.bottom, 5)
                        .foregroundColor(colors.headerMusicCountText.toSColor())
                        .font(.system(size: 15, weight: .medium))
                        .background(content: {
                            
                            
                            ZStack {
                                
                                GeometryReader { geo in
                                    
                                    colors.headerProgress.toSColor()
                                        .clipShape(RoundedRectangle(cornerRadius: 2))
                                        .frame(maxHeight: .infinity)
                                        .frame(width: geo.size.width * CGFloat(viewModel.getRoyaltyFreeTracksLeftProgress()))
                                    
                                }.frame(maxWidth: .infinity, maxHeight: .infinity)
                                
                            }
                            .frame(height: 2)
                            .background(colors.headerProgressTrack.toSColor())
                            .clipShape(RoundedRectangle(cornerRadius: 2))
                            .padding(.top, 24)
                            
                            
                        })
                } else {
                    
                    Text(viewModel.getTextRoyaltyFreeTracksLeft(resToString: { res in
                        res.localized()
                    }))
                        .padding(.bottom, 5)
                        .foregroundColor(colors.headerMusicCountText.toSColor())
                        .font(.system(size: 15, weight: .medium))
                }
                
                
                
            }.padding(.bottom, 14)
        }
        
        
    }.frame(maxWidth: .infinity)
    
}

struct MusicTabContentView_Previews: PreviewProvider {
    static var previews: some View {
        
        let colors = MusicDarkColors()
        let albums = InspResponseData(data: AlbumsResponse(albums: getTestAlbums()))
        //let albums = InspResponseLoading<AlbumsResponse>(progress: nil)
        //let albums = InspResponseError<AlbumsResponse>(throwable: KotlinThrowable(message: "No network available"))
        
        let tracks = InspResponseData(data: TracksResponse(album: getTestAlbums()[0], tracks: getTestTracks()))
        //let tracks = InspResponseLoading<TracksResponse>(progress: nil)
        
        //let tracks = InspResponseError<TracksResponse>(throwable: KotlinThrowable(message: "No network available"))
        
        VStack(alignment: .leading, spacing: 0) {
            
            MusicTabContentView(colors: colors, contentTracks: tracks, contentAlbums: albums, selectedAlbumId: -1, onSelectedAlbumIdChange: { _ in }, onRetryTracksClick: {}, onRetryAlbumsClick: {}, showTracksSearchBar: true, usageWarning: AnyView(royaltyFreeMusicHeader(colors: MusicDarkColors(), hasPremium: false, viewModel: Dependencies.resolveAuto())), tab: MusicTab.library, onNavigationSubscribe: {})
                .environmentObject(MusicPlayerViewModel())
                .environmentObject(MusicDownloadingViewModelApple(Dependencies.resolveAuto()))
        }.background(colors.background.toSColor())
    }
}
