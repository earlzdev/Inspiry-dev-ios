//
//  MusicViewModel.swift
//  MusicFeatureIos
//
//  Created by vlad on 8/4/21.
//

import Foundation
import shared
import os

open class BaseMusicViewModelApple: ViewModelBridge<BaseMusicViewModel> {
    
    @Published
    public private(set) var albumsResponse: InspResponse<AlbumsResponse>
    
    @Published
    public private(set) var tracksResponse: InspResponse<TracksResponse>
    
    @Published
    public private(set) var searchQuery: String?
    
    @Published
    public private(set) var selectedAlbumId: Int64
    
    override init(_ coreModel: BaseMusicViewModel) {
        self.albumsResponse = coreModel.albumsState.value as! InspResponse<AlbumsResponse>
        self.tracksResponse = coreModel.tracksState.value as! InspResponse<TracksResponse>
        self.searchQuery = coreModel.searchQueryState.value as? String
        self.selectedAlbumId = coreModel.selectedAlbumIdState.value as! Int64
        
        super.init(coreModel)
        
        CoroutineUtil.watch(state: coreModel.albumsState, onValueReceived: { [weak self] in self?.albumsResponse = $0 })
        CoroutineUtil.watch(state: coreModel.tracksState, onValueReceived: { [weak self] in self?.tracksResponse = $0 })
        CoroutineUtil.watch(state: coreModel.selectedAlbumIdState, onValueReceived: { [weak self] in self?.selectedAlbumId = $0 })
    }
    
    private var initLoadingCalled: Bool = false
    func mayInitLoading() {
        if !initLoadingCalled {
            initLoadingCalled = true
            coreModel.initialLoading()
        }
    }
}
