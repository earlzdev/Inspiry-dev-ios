//
//  TemplatesList.swift
//  iosApp
//
//  Created by rst10h on 22.12.21.
//

import SwiftUI
import shared

struct CategorizedTemplatesPage: View {
    
    @EnvironmentObject var viewModel: MainScreenViewModelApple
    @EnvironmentObject var topBarBehavior: LinkedScrollBehavior
    
    @StateObject var bannerBehavior = LinkedScrollBehavior(minOffset: -70 - (getBottomScreenInset() * 0.7), maxOffset: 0)
    @State private var visibleCategories: Set<String> = []
    @State private var isUserTapped = false
    
    @EnvironmentObject
    private var licenseManagerWrapper: LicenseManagerAppleWrapper
    
    var body: some View {
        
        let dimens = viewModel.coreModel.getMainScreenDimens()
        
        VStack(spacing: 0) {
            OffsetableScrollView {
                ScrollViewReader { sv in
                    TemplatesList(visibleCategories: $visibleCategories)
                        .padding(.leading, 25)
                        .padding(.trailing, 25)
                        .onChange(of: viewModel.currentCategoryIndex) { _ in
                            if (!isUserTapped) {
                                withAnimation() {
                                    sv.scrollTo(viewModel.mainTemplates[viewModel.currentCategoryIndex].id, anchor: .top)
                                }
                            }
                            
                        }
                        .environmentObject(viewModel)
                    
                }
            }
            .onScroll { offset in
                if (isUserTapped) {
                    bannerBehavior.parentScrollChanged(newScrollPosition: offset)
                    topBarBehavior.parentScrollChanged(newScrollPosition: offset)
                    if let cat = visibleCategories.first {
                        if let index = viewModel.templateCategories.firstIndex(where: { $0.id == cat }) {
                            viewModel.coreModel.changeCategory(newIndex: Int32(index))
                        }
                    }
                    
                }
            }
            .simultaneousGesture(
                DragGesture()
                    .onChanged { _ in
                        isUserTapped = true
                    }
            )
            ZStack {
                if (viewModel.bannerVisible == true && !licenseManagerWrapper.hasPremium) {
                    NavigationLink(
                        destination: SubscribeUIView(source: "BottomBanner", onNavigationBack: {
                            viewModel.fullScreenSelection = nil
                        })
                        .navigationBarHidden(true), tag: "subscribe", selection: $viewModel.fullScreenSelection) {
                            BottomBannerPremium()
                                .environmentObject(viewModel)
                        }
                        .offset(y: bannerBehavior.currentOffset)
                }
                BottomCategoriesPanel(userScroll: $isUserTapped)
                    .clipped()
                    .shadow(color: Color.black.opacity(0.1), radius: 3, x: 0, y: -3)
                    .environmentObject(viewModel)
            }
        }
        .padding(.top, topBarBehavior.currentOffset + CGFloat(dimens.topBarHeight + dimens.topBarPadding))
        .ignoresSafeArea(edges: .bottom)
        //        .onDisappear {
        //memory leak test
        //            print("clear templates")
        //            viewModel.templateModelsCache.removeAll()
        //        }
    }
}

struct CategorizedTemplatesPage_Preview: PreviewProvider {
    
    static var previews: some View {
        CategorizedTemplatesPage()
            .environmentObject(MainScreenViewModelApple.Create())
            .environmentObject(LinkedScrollBehavior(minOffset: 0, maxOffset: 0))
    }
}
