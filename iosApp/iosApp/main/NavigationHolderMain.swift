//
//  EmptyNavigationMain.swift
//  iosApp
//
//  Created by rst10h on 2.02.22.
//

import SwiftUI

struct NavigationHolderMain: View {
    @EnvironmentObject
    var viewModel: MainScreenViewModelApple
    var body: some View {
        NavigationLink(
            destination: OnboardingMain(fullScreenSelection: $viewModel.fullScreenSelection)
                .navigationBarHidden(true), tag: Constants.TAG_ONBOARDING_VIEW,
            selection: $viewModel.fullScreenSelection) { EmptyView() }
        NavigationLink(
            destination: SubscribeUIView(source: Constants.TAG_ONBOARDING_VIEW) { viewModel.fullScreenSelection = nil }
                .navigationBarHidden(true), tag: Constants.TAG_SUBSCRIBE_VIEW,
            selection: $viewModel.fullScreenSelection) { EmptyView() }
    }
}
