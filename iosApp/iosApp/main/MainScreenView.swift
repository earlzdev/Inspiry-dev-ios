//
//  MainScreenView.swift
//  iosApp
//
//  Created by rst10h on 20.12.21.
//

import SwiftUI
import shared

struct MainScreenView: View {
          
    @StateObject
    private var viewModel = MainScreenViewModelApple.Create()
    
    @StateObject
    private var licenseManagerWrapper: LicenseManagerAppleWrapper = LicenseManagerAppleWrapper(coreModel: Dependencies.resolveAuto())
    
    @StateObject
    var topBarBehavior = LinkedScrollBehavior(minOffset: -50, maxOffset: 0)
    
    init() {
        UIScrollView.appearance().bounces = false
    }
    
    var body: some View {
        let dimens = viewModel.coreModel.getMainScreenDimens()
        
        if (viewModel.isLoaded) {
            
            NavigationView {
                
                ZStack(alignment: .top) {
                    VStack {
                        if (viewModel.currentPage == MainScreenPages.templates) {
                            CategorizedTemplatesPage ()
                            
                        } else
                        if (viewModel.currentPage == MainScreenPages.story) {
                            MyStoriesRoot()
                        } else
                        if (viewModel.currentPage == MainScreenPages.pro) {
                            StartView()
                        }
                        else {
                            Spacer()
                        }
                        
                        NavigationHolderMain()
                    }
                    
                    MainTopTabs(menuItems: viewModel.topTabsItems(), colors: TopTabColorsLight(), activePage: viewModel.currentPage ?? .test, onTabSelected: {page in
                        if (page == MainScreenPages.pro) {
                            viewModel.fullScreenSelection = Constants.TAG_SUBSCRIBE_VIEW
                        } else {
                            viewModel.selectNewPage(newPage: page)
                        }
                    })
                    .offset(y: topBarBehavior.currentOffset)
                    .frame(maxHeight: CGFloat(dimens.topBarHeight))
                    .clipped()
                    
                    if (viewModel.feedbackDialogVisible) {
                        Color(red: 0, green: 0, blue: 0, opacity: 0.4)
                            .ignoresSafeArea()
                            .zIndex(98)
                            .onTapGesture {
                                withAnimation {
                                    viewModel.feedbackDialogVisible.toggle()
                                }
                            }
                        SupportDialog(isVisible: $viewModel.feedbackDialogVisible)
                            .zIndex(99)
                    }
                    if (viewModel.templateActionsDialogVisible) {
                        Color(red: 0, green: 0, blue: 0, opacity: 0.01)
                            .ignoresSafeArea()
                            .zIndex(98)
                            .onTapGesture {
                                withAnimation {
                                    viewModel.templateActionsDialogVisible.toggle()
                                }
                            }
                        TemplatesActionDialog(templateName: viewModel.selectedTemplateName) { action, newName in
                            viewModel.updateTemplateName(newName ?? "")
                            viewModel.myTemplateAction(action)
                        }
                        .zIndex(99)
                    }
                }
                .padding(.top, UIScreen.statusBarHeight < Constants.TOP_SAFE_AREA_MIN_SIZE ? Constants.TOP_SAFE_AREA_MIN_SIZE - UIScreen.statusBarHeight : 0)
                .frame(maxHeight: .infinity)
                .navigationBarHidden(true)
            }
            .clipped()
            .environmentObject(licenseManagerWrapper)
            .environmentObject(viewModel)
            .environmentObject(topBarBehavior)
            .ignoresSafeArea(.all)
            .statusBarStyle(.darkContent)
            .preferredColorScheme(.light)
        }
        else {
            SplashScreen()
        }
    }
    
}


struct MainScreenView_Previews: PreviewProvider {
    static var previews: some View {
        Group {
            MainScreenView()
        }
    }
}

