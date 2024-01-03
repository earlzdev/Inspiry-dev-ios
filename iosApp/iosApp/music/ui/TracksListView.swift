//
//  TracksListView.swift
//  MusicFeatureIos
//
//  Created by vlad on 7/4/21.
//

import SwiftUI
import shared

struct TracksListView: View {
    
    let colors: MusicColors
    let tracks: [Track]
    let album: Album
    let showSearchBar: Bool
    let tab: MusicTab
    let onNavigatioSubscribe: () -> ()
    
    @EnvironmentObject
    var musicPlayerViewModel: MusicPlayerViewModel
    
    @State
    var searchQuery: String = ""
    
    var body: some View {
        
        let currentTracks = searchQuery.isEmpty ? tracks : TrackUtils.init().filterTracks(tracks: tracks, query: searchQuery)
        
        ScrollView {
            LazyVStack(spacing: 0) {
                
                if (showSearchBar) {
                    MusicSearchBar(colors: colors, searchQuery: $searchQuery)
                }
                
                ForEach(currentTracks, id: \.self.url) { track in
                    
                    let isSelected = track.url == musicPlayerViewModel.selectedTrack
                    
                    
                    if isSelected {
                        SelectedTrackContentView(colors: colors, track: track, album: album, onClickPlayPause: {
                            
                            
                            musicPlayerViewModel.isPlaying ? musicPlayerViewModel.pause() : musicPlayerViewModel.play()
                            
                        }, tab: tab, onNavigatioSubscribe: onNavigatioSubscribe)
                        .padding(.vertical, 10)
                        .background(colors.trackSelectedBg.toSColor())
                        
                    } else {
                        
                        Button(action: {
                            musicPlayerViewModel.playTrack(url: track.url)
                        }, label: {
                            
                            UnselectedTrackContent(colors: colors, track: track)
                                .frame(height: 64, alignment: .leading)
                                .frame(maxWidth: .infinity)
                            
                                .background(isSelected ? colors.trackSelectedBg.toSColor() : .clear)
                        })
                    }
                }
            }
        }.onDisappear(perform: {
            musicPlayerViewModel.pause()
        })
        
    }
}

extension Array where Element: Any {
    func duplicate(times: Int32) -> [Element] {
        
        var newArr = [Element].self.init()
        
        (0...times-1).forEach { _ in
            newArr.append(contentsOf: self)
        }
        
        return newArr
    }
}

func getTestTracks() -> [Track] {
    return [
        Track(url: "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview124/v4/ab/15/31/ab1531f7-be5a-4be1-a007-113e6f87f235/mzaf_16276103177644625463.plus.aac.p.m4a", title: "BLIND (feat. Young Thug)", artist: "DaBaby", image: "https://is2-ssl.mzstatic.com/image/thumb/Music124/v4/24/b7/1b/24b71bab-67bb-5db7-feca-a5a32dde6211/20UMGIM66717.rgb.jpg/{w}x{h}bb.jpeg"),
        Track(url: "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview124/v4/e1/9b/98/e19b98ac-c6e6-650d-0dbf-a90b30f5661b/mzaf_17403832288932645228.plus.aac.p.m4a", title: "Move Ya Hips (feat. Nicki Minaj & MadeinTYO)", artist: "A$AP Ferg", image: "https://is5-ssl.mzstatic.com/image/thumb/Music124/v4/35/74/37/35743752-f085-130b-ee86-df35d146f3b0/886448647508.jpg/{w}x{h}bb.jpeg"),
        Track(url: "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview114/v4/3c/27/c3/3c27c3df-b6e8-56fd-598a-6d5bbd66ca5f/mzaf_11443864963949443746.plus.aac.p.m4a", title: "Out for the night", artist: "Pop Smoke", image: "https://is5-ssl.mzstatic.com/image/thumb/Music114/v4/28/14/7e/28147e2a-a84d-84b0-fc9d-f33f7e9b3561/20UMGIM61787.rgb.jpg/{w}x{h}bb.jpeg"),
        Track(url: "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview114/v4/d9/66/d4/d966d465-504f-76eb-0961-3581892b44eb/mzaf_9386402732957380981.plus.aac.p.m4a", title: "Cafeteria (feat. Gunna)", artist: "CHASE B & Don Toliver", image: nil),
        Track(url: "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview128/v4/9b/84/50/9b845084-9d76-977f-7e6c-43ef43a6d707/mzaf_6453149419518624614.plus.aac.p.m4a", title: "Mirrors (Radio Edit)", artist: "Justin Timberlake", image: "https://is2-ssl.mzstatic.com/image/thumb/Music128/v4/61/04/1c/61041cfa-2f8b-26d1-f807-9d7f09a0d7e2/886443898325.jpg/{w}x{h}bb.jpeg"),
        Track(url: "https://audio-ssl.itunes.apple.com/itunes-assets/Music2/v4/71/bb/49/71bb4999-9386-3feb-1e48-a4139a5833e4/mzaf_210119998254061436.plus.aac.p.m4a", title: "Love On Top", artist: "Beyoncé", image: "https://is5-ssl.mzstatic.com/image/thumb/Music2/v4/d1/31/f5/d131f596-5287-9844-66fe-c5a6d74e0557/886443938373.jpg/{w}x{h}bb.jpeg"),
        
        Track(url: "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview118/v4/81/02/bf/8102bf56-79bb-39b5-feb6-8f6e56640925/mzaf_6424710951991717759.plus.aac.p.m4a", title: "Love on the Brain", artist: "Rihanna", image: "https://is2-ssl.mzstatic.com/image/thumb/Music128/v4/a0/b9/61/a0b96196-1bcc-7574-3ff3-f1a53493e656/00851365006516.rgb.jpg/{w}x{h}bb.jpeg"),
        Track(url: "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview113/v4/96/72/f0/9672f0d2-f188-7dbf-afe1-6cf92ec1a4d6/mzaf_5250567638374541976.plus.aac.p.m4a", title: "The Bones", artist: "Maren Morris & Hozier", image: "https://is5-ssl.mzstatic.com/image/thumb/Music123/v4/1e/fa/ed/1efaed1c-05af-74ee-5835-61c924709ec9/886447991480.jpg/{w}x{h}bb.jpeg"),
        Track(url: "https://audio-ssl.itunes.apple.com/itunes-assets/AudioPreview82/v4/5d/71/ef/5d71efb7-2c40-0490-f137-9b86372f8fd5/mzaf_7873689062499888403.plus.aac.p.m4a", title: "Something Just Like This", artist: "The Chainsmokers & Coldplay", image: "https://is4-ssl.mzstatic.com/image/thumb/Music122/v4/43/0b/d5/430bd507-bb4a-c26a-fdbc-98f713d728c1/886446379289.jpg/{w}x{h}bb.jpeg"),
        
    ]
}

func getTestAlbums() -> [Album] {
    return [Album(id: 1, name: "HipHop", artist: "PopSmoke", tracksCount: 20, image: "https://is5-ssl.mzstatic.com/image/thumb/Music114/v4/28/14/7e/28147e2a-a84d-84b0-fc9d-f33f7e9b3561/20UMGIM61787.rgb.jpg/{w}x{h}bb.jpeg"), Album(id: 2, name: "Classical", artist: "PopSmoke", tracksCount: 20, image: "https://is2-ssl.mzstatic.com/image/thumb/Music124/v4/24/b7/1b/24b71bab-67bb-5db7-feca-a5a32dde6211/20UMGIM66717.rgb.jpg/{w}x{h}bb.jpeg")]
}

struct TracksListView_Previews: PreviewProvider {
    static var previews: some View {
        let colors = MusicDarkColors()
        let selectedTrack = getTestTracks()[0]
        TracksListView(colors: colors, tracks: getTestTracks(), album: getTestAlbums()[0], showSearchBar: true, tab: MusicTab.library, onNavigatioSubscribe: {}).background(colors.background.toSColor())
            .environmentObject(MusicPlayerViewModel(isPlaying: false, selectedTrack: selectedTrack.url))
            .environmentObject(MusicDownloadingViewModelApple(Dependencies.resolveAuto()))
    }
}
