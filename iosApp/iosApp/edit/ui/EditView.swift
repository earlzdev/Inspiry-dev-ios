//
//  EditView.swift
//  iosApp
//
//  Created by vlad on 2/11/21.
//

import SwiftUI
import shared

struct EditView: View {
    let templatePath: TemplatePath
    let originalData: OriginalTemplateData?
    let onNavigationBack: (TemplatePath, Bool) -> ()
    let colors: EditColors = EditColorsLight()
    let dimens: EditDimens = EditDimensPhone()
    
    @StateObject
    var viewModel: EditViewModelApple

    @State
    var isLoading = false
    
    init( templatePath: TemplatePath, originalData: OriginalTemplateData?,
          onNavigationBack: @escaping (TemplatePath, Bool) -> ()) {
        self.templatePath = templatePath
        self.originalData = originalData
        self.onNavigationBack = onNavigationBack
        self._viewModel = StateObject(wrappedValue: EditViewModelApple(templatePath: templatePath, initialOriginalData: originalData))
    }
    
    func onClickBack() {
        viewModel.onClickBack { saved in
            onNavigationBack(templatePath, saved)
        }
    }
    
    var body: some View {
        NavigationView {
            
            // TODO: get value from shared EditViewModel
            let displayProLabel = viewModel.showProWatermark
            ZStack {
                (viewModel.editMainState == .Preview ? Color.black : Color.white)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .ignoresSafeArea(edges: .bottom)
                
                NavigationHolderEdit()
                
                VStack {
                    if viewModel.editMainState == .Export || viewModel.editMainState == .Edit {
                        EditTopBarView(
                            colors: colors,
                            dimens: dimens,
                            displayExportButton: viewModel.editMainState == .Edit,
                            displayProButton: displayProLabel,
                            displayDoneButton: viewModel.isCloseablePanelOpened,
                            onNavigationBack: onClickBack,
                            fullScreenSelection: $viewModel.fullScreenSelection)
                        .environmentObject(viewModel)
                        .zIndex(100.0)
                        Spacer(minLength: 0)
                    } else if viewModel.editMainState == .Preview {
                        Spacer().frame(height: 0)
                    }
                    if (viewModel.editMainState != .Export) {
                        ZStack {
                            TemplateUIView(templateModel: viewModel.templateView, autoplay: false, playSound: false)
                        }
                        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
                        .cornerRadius(viewModel.editMainState == .Preview ? 0 : 16)
                        .aspectRatio(
                            CGFloat( viewModel.formatState.aspectRatio() ), contentMode: ContentMode.fit)
                        .padding(.horizontal, dimens.editTemplateHorizontalPadding.cg) //may remove it
                        .clipped()
                        .shadow(color: viewModel.editMainState == .Preview ? Color.clear : Color.init(red: 0, green: 0, blue: 0, opacity: 0.20), radius: 4, x: 0, y: 0)
                        .animation(.easeInOut)
                        .overlay(
                            SelectedViewBorder()
                                .environmentObject(viewModel.wrapperHelper)
                                .zIndex(300)
                        )
                        .onTapGesture {
                            //viewModel.templateView.changeSelectedView(value: nil)
                        }

                    }
                    if (viewModel.editMainState == .Edit) {
                        Spacer(minLength: 0)
                        InstrumentsBottomPanel(colors: colors, dimens: dimens).ignoresSafeArea()
                            .environmentObject(viewModel)
                    } else if (viewModel.editMainState == .Export) {
                        ExportBottomPanel(colors: colors, dimens: dimens, viewModel: viewModel.getExportViewModel())
                    }
                    
                }.frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
                    .navigationBarHidden(true)
                //.statusBar(hidden: viewModel.editMainState == .Preview)
                
                
                if viewModel.editMainState == .Preview {
                    // TODO: use correct template duration? Or just obtain value from a templateView
                    
                    PreviewOverlayUI(displayProLabel: displayProLabel, templateView: viewModel.templateView, onBackPressed: onClickBack)
                }
                
                if (viewModel.waitLoading) {
                    RadialGradient(colors: [Color.white, Color.white.opacity(0.5)], center: .center, startRadius: 50, endRadius: 200)
                    if let progress = viewModel.loadingProgress {
                        VStack {
                            ProgressView("\(MR.strings().media_loading_message.localized()) \(MR.strings().from_icloud.localized())")
                                .progressViewStyle(CircularProgressViewStyle())
                            ZStack {
                                GeometryReader { geometry in
                                    
                                    ZStack {
                                        LinearGradient(colors: [colors.exportProgressStart.toSColor(), colors.exportProgressEnd.toSColor()], startPoint: .leading, endPoint: .trailing)
                                        
                                    }.frame(width: geometry.size.width * CGFloat(progress), height: 4)
                                        .clipShape(RoundedCorner(radius: 4))
                                    
                                }.frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
                                    .padding(.top, 1)
                                    .padding(.horizontal, 1)
                            }
                            .frame(maxWidth: .infinity)
                            .frame(width: 150, height: 6)
                        }
                    } else {
                        ProgressView("\(MR.strings().media_loading_message.localized())...")
                            .progressViewStyle(CircularProgressViewStyle())
                    }
                }
                
                if (viewModel.keyboardIsShown) {
                    EditTextScreen(
                        colors: colors,
                        dimens: dimens,
                        text: viewModel.getTextForEdit(),
                        font: viewModel.getFontForEdit(),
                        onCancel: {viewModel.coreModel?.isKeyboardShown.setValue(false)},
                        onDone: { new in viewModel.onTextChanged(newText: new)}
                    )
               }
                if (viewModel.removeBGLoading) {
                    RemoveBackgroundView() {
                        viewModel.removeBGLoading = false // onCanceled
                    }
                }
                if (viewModel.saveConfirmationDialog) {
                    SaveDialog(isVisible: $viewModel.saveConfirmationDialog, positiveAction: {
                        viewModel.saveAndExit()
                        onNavigationBack(templatePath, true)
                    }, negativeAction: {
                        onNavigationBack(templatePath, false)
                    })
                        .transition(.opacity.animation(.easeInOut))
                }
            }.ignoresSafeArea(edges: viewModel.keyboardIsShown ? .all : .bottom)
                .environmentObject(viewModel)
        }
        .preferredColorScheme(viewModel.isDarkTheme ? .dark : .light)
        .sheet(isPresented: $viewModel.galleryIsShown) {
            MediaPickerUI(mediaResult: $viewModel.mediaResult, isActive: $viewModel.galleryIsShown, isLoading: $viewModel.waitLoading, iCloudProgress: $viewModel.loadingProgress, maxMediasCount: viewModel.coreModel?.getPickImageConfig().maxSelectable.int ?? 1)
        }
        .alert(isPresented: $viewModel.notAuthorizedDialog) {
            Alert(data: AlertData.getMediaAlert())
        }
    }
}

struct EditView_Previews: PreviewProvider {
    static var previews: some View {
        let res = MR.assetsTemplatesGrid().Grid3x1Template
        let path = PredefinedTemplatePath(res: res)
        EditView(templatePath: path, originalData: nil, onNavigationBack: { _, _ in })
    }
}
