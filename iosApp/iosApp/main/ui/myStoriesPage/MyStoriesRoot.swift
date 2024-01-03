//
//  MyStoriesRoot.swift
//  iosApp
//
//  Created by rst10h on 31.12.21.
//

import SwiftUI
import shared

struct MyStoriesRoot: View {
 
    @EnvironmentObject var viewModel: MainScreenViewModelApple
    @EnvironmentObject var topBarBehavior: LinkedScrollBehavior
       
    var body: some View {
        let dimens = viewModel.coreModel.getMainScreenDimens()
        let isEmpty = viewModel.myStoriesViewModel.templatesCache.isEmpty
        VStack {
            OffsetableScrollView {
                ScrollViewReader { sv in
                    VStack {
                        MyStoriesList(storiesModel: viewModel.myStoriesViewModel)
                            .padding(.leading, 25)
                            .padding(.trailing, 25)
                            .environmentObject(viewModel)
                        InstagramLink()
                            .padding(.top, isEmpty ? 10 : 25)
                        if (isEmpty) {
                            EmptyMyStories()
                                .padding(.top, 100)
                            
                        }
                        Spacer()
                            .padding(.bottom, 50)
                    }
                }
            }
            .onScroll { offset in
                topBarBehavior.parentScrollChanged(newScrollPosition: offset)
            }
            .overlay(
                Button (action: {
                    withAnimation {
                        viewModel.feedbackDialogVisible.toggle()
                    }
                }, label: {
                    CyborgImage(name: "ic_btn_support")
                        .scaledToFill()
                        .frame(width:  40, height: 40)
                        .clipShape(Circle())
                        .shadow(color: Color.black.opacity(0.1), radius: 15)
                        .padding(.horizontal, 30)
                        .padding(.vertical, 30)
                })
                , alignment: .bottomTrailing
            )
        }
        .clipped()
        .padding(.top, topBarBehavior.currentOffset + CGFloat(dimens.topBarHeight + dimens.topBarPadding))
        .onAppear {
            //viewModel.loadMyStories()
        }
        .onDisappear {
            viewModel.onDisappearPage(.story, scrollBehavior: topBarBehavior)
        }
    }
}

struct MyStoriesRoot_Preview: PreviewProvider {
    
    static var previews: some View {
        MyStoriesRoot()
            .environmentObject(MainScreenViewModelApple.Create())
            .environmentObject(LinkedScrollBehavior(minOffset: 0, maxOffset: 0))
    }
}
