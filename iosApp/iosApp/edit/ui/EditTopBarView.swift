//
//  EditTopBarView.swift
//  iosApp
//
//  Created by vlad on 12/1/22.
//

import SwiftUI
import shared

struct EditTopBarView: View {
    
    let colors: EditColors
    let dimens: EditDimens
    let displayExportButton: Bool
    let displayProButton: Bool
    let displayDoneButton: Bool
    let onNavigationBack: () -> ()
    
    @Binding
    var fullScreenSelection: String?
    
    @EnvironmentObject
    var viewModel: EditViewModelApple
    
    
    var body: some View {
        ZStack {
            
            HStack {
                
                TopBarBackView(colors: colors, dimens: dimens, onNavigationBack: onNavigationBack)
                
                Spacer().frame(maxWidth: .infinity, maxHeight: .infinity)
                
                if (displayExportButton && !displayDoneButton) {
                    
                    if displayProButton {
                        
                        // TODO: get source from shared EditViewModel
                        //                        NavigationLink(destination: SubscribeUIView(source: "temp_source", onNavigationBack: onNavigationBack), tag: StartView.TAG_SUBSCRIBE_VIEW, selection: $fullScreenSelection) {
                        Button(action: {viewModel.updateFullScreenTool(fullScreenItem: .subscribe)}, label: {
                            
                            ZStack(alignment: .bottomTrailing) {
                                
                                Text(MR.strings().save.localized())
                                    .font(.system(size: CGFloat(dimens.topBarTextSize), weight: .bold))
                                    .foregroundColor(colors.sharePremiumText.toSColor())
                                    .lineLimit(1)
                                    .padding(.horizontal, 20)
                                    .frame(height: 32)
                                    .background(colors.sharePremiumBg.toSColor())
                                    .clipShape(RoundedRectangle(cornerRadius: 10))
                                    .fixedSize()
                                
                                Image("ic_pro_share").offset(x: 5, y: 3)
                                
                            }

                               })
                        .padding(.trailing, 14)
                            .frame(maxHeight: .infinity)
                            .onLongPressGesture(maximumDistance: 10000, perform: {
                                if Dependencies.isDebug() {
                                    viewModel.onClickExport()
                                }
                            })
                        
                        
                    } else {
                        Button(action: {
                            viewModel.onClickExport()
                            
                        }) {
                            Text(MR.strings().save.localized())
                                .font(.system(size: CGFloat(dimens.topBarTextSize), weight: .bold))
                                .foregroundColor(colors.topBarText.toSColor())
                                .lineLimit(1)
                                .padding(.horizontal, 16)
                                .fixedSize()
                            
                        }
                        .padding(.trailing, 14)
                        .frame(maxHeight: .infinity)
                    }
                    
                }
                if displayDoneButton {
                    Button(action: {
                        viewModel.onClickDone()
                        
                    }) {
                        Text(MR.strings().done.localized())
                            .font(.system(size: CGFloat(dimens.topBarTextSize), weight: .bold))
                            .foregroundColor(colors.sharePremiumText.toSColor())
                            .lineLimit(1)
                            .padding(.horizontal, 16)
                            .fixedSize()
                        
                    }
                    .padding(.trailing, 14)
                    .frame(maxHeight: .infinity)
                }
            }
            
            if viewModel.editMainState == .Edit {
                Button(action: {
                    viewModel.onClickPreview()
                    
                }, label: {
                    
                    SVGImage(svgName: "ic_preview_template")
                    
                }) .frame(maxHeight: .infinity)
                    .frame(width: 60)
            }
            
            
        }.frame(height: CGFloat(dimens.topBarHeight))
    }
}

struct EditTopBarView_Previews: PreviewProvider {
    
    
    static var previews: some View {
        let fullScreenSelection = Binding<String?>(get: { nil }, set: { _ in })
        
        EditTopBarView(colors: EditColorsLight(), dimens: EditDimensPhone(), displayExportButton: true, displayProButton: true, displayDoneButton: true, onNavigationBack: {}, fullScreenSelection: fullScreenSelection)
            .environmentObject(EditViewModelApple.modelForPreviews())
    }
}
