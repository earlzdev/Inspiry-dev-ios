//
//  EmptyNavigationEdit.swift
//  iosApp
//
//  Created by rst10h on 2.02.22.
//

import Foundation

import SwiftUI

struct NavigationHolderEdit: View {
    @EnvironmentObject
    var viewModel: EditViewModelApple
    var body: some View {
        NavigationLink(
            destination: SubscribeUIView(source: Constants.TAG_EDIT_VIEW) {
                viewModel.fullScreenSelection = nil
            }
                .navigationBarHidden(true), tag: Constants.TAG_SUBSCRIBE_VIEW,
            selection: $viewModel.fullScreenSelection) { EmptyView() }
        
        NavigationLink(
            destination: StickersListUI(model: viewModel.getStickersModel()) { newSticker in
                if let newSticker = newSticker {
                    viewModel.coreModel?.onStickerResult(media: newSticker.media)
                    viewModel.backToEdit()
                    viewModel.templateView.setFrameForEdit()
                    viewModel.notifyTemplateChanged()
                } else {
                    viewModel.backToEdit()
                }
                
            }
                .navigationBarHidden(true),
            tag: Constants.TAG_STICKERS_VIEW,
            selection: $viewModel.fullScreenSelection) { EmptyView() }
        
        NavigationLink(
            destination: TextAnimationsUI(model: viewModel.getTextAnimModel() ){
                path in
                let selected = viewModel.templateView.selectedView?.asInspTextView()
                viewModel.coreModel?.onTextPicked(returnTextHere: selected, textAnimationResult: path, completionHandler: { _, _ in
                    viewModel.backToEdit()
                }
                )
            }
                .navigationBarHidden(true),
            tag: Constants.TAG_ANIMATION_VIEW,
            selection: $viewModel.fullScreenSelection) { EmptyView() }
        NavigationLink(
            destination: MainMusicView(onNavigationBack: {
                viewModel.backToEdit()
            }, onPickedMusic: { templateMusic in
                viewModel.analyticsManager.onMusicPickedFromLibrary(music: templateMusic)
                viewModel.templateView.releaseMusic()
                viewModel.coreModel?.setMusic(music: templateMusic)
                viewModel.backToEdit()
            })
                .navigationBarHidden(true),
            tag: Constants.TAG_MUSIC_LIBRARY,
            selection: $viewModel.fullScreenSelection) {
                
                EmptyView()
            }
    }
}
