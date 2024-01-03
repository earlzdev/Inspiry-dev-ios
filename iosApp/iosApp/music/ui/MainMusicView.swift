//
//  MainMusicView.swift
//  MusicFeatureIos
//
//  Created by vlad on 7/4/21.
//

import SwiftUI
import shared
import MediaPlayer

struct MainMusicView: View {
    
    private let colors: MusicColors = MusicDarkColors()
    
    private let remoteConfig = Dependencies.diContainer.resolve(InspRemoteConfig.self)!
    
    private let showItunesTab: Bool
    @State private var currentItem: MusicTab
    
    @StateObject
    private var itunesViewModel = BaseMusicViewModelApple( BaseMusicViewModel(initialAlbumId: -1, initLoadingOnCreate: false, provider: Dependencies.diContainer.resolve(ITunesMusicLibraryProvider.self)!, loggerGetter: Dependencies.diContainer.resolve(LoggerGetter.self)!))
    
    @StateObject
    private var libraryViewModel = BaseMusicViewModelApple( BaseMusicViewModel(initialAlbumId: -1, initLoadingOnCreate: false, provider: Dependencies.diContainer.resolve(RemoteLibraryMusicProvider.self)!, loggerGetter: Dependencies.diContainer.resolve(LoggerGetter.self)!))
    
    @StateObject
    private var myMusicViewModel = BaseMusicViewModelApple( BaseMusicViewModel(initialAlbumId: -1, initLoadingOnCreate: false, provider: Dependencies.diContainer.resolve(LocalMusicLibraryProvider.self)!, loggerGetter: Dependencies.diContainer.resolve(LoggerGetter.self)!))
    
    @StateObject
    private var downloadingViewModel: MusicDownloadingViewModelApple = MusicDownloadingViewModelApple(Dependencies.resolveAuto())
    
    @StateObject
    private var musicPlayerViewModel = MusicPlayerViewModel()
    
    @EnvironmentObject
    private var licenseManagerWrapper: LicenseManagerAppleWrapper
    
    @State
    private var audioLibraryAuthorized: Bool = false
    
    @State
    private var selectionScreen: String? = nil
    
    private let onNavigationBack: () -> ()
    private let onPickedMusic: (TemplateMusic) -> ()
    
    init(onNavigationBack: @escaping () -> (), onPickedMusic: @escaping (TemplateMusic) -> ()) {
        self.onNavigationBack = onNavigationBack
        self.onPickedMusic = onPickedMusic
        
        showItunesTab  = remoteConfig.getBoolean(key: "itunes_music")
        _currentItem = State(initialValue: showItunesTab ? MusicTab.itunes : MusicTab.library)
        
        print("mainMusicView created \(getTestTracks().count)")
    }
    
    func TabContent(viewModel: BaseMusicViewModelApple, showTracksSearchBar: Bool, usageWarning: AnyView?, tab: MusicTab) -> MusicTabContentView {
        
        viewModel.mayInitLoading()
        
        return MusicTabContentView(
            colors: colors,
            contentTracks: viewModel.tracksResponse,
            contentAlbums: viewModel.albumsResponse,
            selectedAlbumId: viewModel.selectedAlbumId,
            onSelectedAlbumIdChange: { newId in
                viewModel.coreModel.loadTrackOnClickAlbum(albumId: newId)
            },
            onRetryTracksClick: {
                viewModel.coreModel.retryTracksOnError()
            },
            onRetryAlbumsClick: {
                viewModel.coreModel.retryAlbumsOnError()
            },
            showTracksSearchBar: showTracksSearchBar,
            usageWarning: usageWarning, tab: tab, onNavigationSubscribe: {
                self.selectionScreen = "subscribe"
            })
    }
    
    func requestAudioAuth() {
        MPMediaLibrary.requestAuthorization { (status) in
            
            audioLibraryAuthorized = status == .authorized
        }
    }
    
    var body: some View {
        
        NavigationView {
            ZStack {
                Color(colors.background.toSColor().cgColor ?? CGColor.init(gray: 0, alpha: 0))
                    .ignoresSafeArea()
                VStack(alignment: .leading, spacing: 0) {
                    
                    NavigationLink(destination: SubscribeUIView(source: BaseSubscribeViewModelKt.SUBSCRIBE_SOURCE_ROYALTY_FREE_MUSIC, onNavigationBack: {
                        self.selectionScreen = nil
                    }), tag: "subscribe", selection: $selectionScreen) { EmptyView() }
                    
                    MusicToolbarView(currentItem: currentItem, colors: colors, showItunesTab: showItunesTab, onCurrentItemChange: { it in
                        
                        if it == MusicTab.myMusic && !audioLibraryAuthorized {
                            requestAudioAuth()
                        } else {
                            currentItem = it
                        }
                        
                    }, onExit: {
                        self.onNavigationBack()
                    })
                    
                    if (currentItem == MusicTab.itunes) {
                        
                        TabContent(viewModel: itunesViewModel, showTracksSearchBar: false, usageWarning: AnyView(warningItunes(colors: colors)), tab: currentItem)
                            .environmentObject(downloadingViewModel)
                        
                    } else if (currentItem == MusicTab.library) {
                        
                        TabContent(viewModel: libraryViewModel, showTracksSearchBar: false, usageWarning: AnyView(royaltyFreeMusicHeader(colors: colors, hasPremium: licenseManagerWrapper.hasPremium, viewModel: downloadingViewModel.coreModel)), tab: currentItem)
                            .environmentObject(downloadingViewModel)
                        
                    } else if (currentItem == MusicTab.myMusic) {
                        
                        TabContent(viewModel: myMusicViewModel, showTracksSearchBar: true, usageWarning: nil, tab: currentItem)
                            .environmentObject(downloadingViewModel)
                    }
                    Spacer().frame(height: 1) //to prevent out of bounds (when list items in safe area)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
                .environmentObject(musicPlayerViewModel)
                .onReceive(NotificationCenter.default.publisher(for: UIApplication.willResignActiveNotification)) { _ in
                    
                    if musicPlayerViewModel.isPlaying {
                        musicPlayerViewModel.pause()
                    }
                }
                
                
                let d = downloadingBlock()
                if d != nil {
                    d
                }
                
            }.frame(maxWidth: .infinity, maxHeight: .infinity)
                .navigationBarTitle("")
                    .navigationBarHidden(true)
            

        }
        .statusBarStyle(.lightContent)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
            .preferredColorScheme(.dark)
        
    }
    
    func progressOverlay(progress: Float) -> AnyView {
        let v = ZStack {
            
            VStack {
                Text(MR.strings.init().music_progress_downloading_message.localized())
                    .foregroundColor(colors.albumTextActive.toSColor())
                    .font(.system(size: 14))
                    .padding(.horizontal, 10)
                    .padding(.bottom, 12)
                
                ProgressView(value: progress, total: 1.0)
                    .progressViewStyle(CircularProgressViewStyle(tint: colors.searchEditCursor.toSColor()))
            }.padding(.horizontal, 25)
                .padding(.vertical, 15)
                .clipShape(RoundedRectangle(cornerRadius: 5))
                .background(colors.downloadDialogBg.toSColor())
                .shadow(color: Color(red: 0, green: 0, blue: 0, opacity: 0.2), radius: 5)
           
        }.frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(Color(red: 0, green: 0, blue: 0, opacity: 0.6))
        
        
        return AnyView(v)
    }
    
    func downloadingBlock() -> AnyView? {
        if downloadingViewModel.downloadingState is InspResponseNothing {
            
        } else {
            if (downloadingViewModel.downloadingState is InspResponseLoading) {
                
                let floatProgress = (downloadingViewModel.downloadingState as! InspResponseLoading<TemplateMusic>).progress ?? 0.0
                
                return progressOverlay(progress: Float(truncating: floatProgress))
                
            } else {
                if (downloadingViewModel.downloadingState is InspResponseData) {
                    let data = (downloadingViewModel.downloadingState as! InspResponseData<TemplateMusic>).data
                    
                    onPickedMusic(data!)
                    
                } else if (downloadingViewModel.downloadingState is InspResponseError) {
                    let error = (downloadingViewModel.downloadingState as! InspResponseError<TemplateMusic>).throwable
                    
                    error.printStackTrace()
                }
                
                downloadingViewModel.coreModel.onPickMusicHandled()
            }
        }
        return nil
    }
    
}


struct MainMusicView_Previews: PreviewProvider {
    static var previews: some View {
        MainMusicView(onNavigationBack: {
            
        }, onPickedMusic: { music in
            
        })
    }
}
