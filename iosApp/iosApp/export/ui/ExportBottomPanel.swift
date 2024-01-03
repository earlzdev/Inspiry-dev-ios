//
//  ExportBottomPanel.swift
//  iosApp
//
//  Created by vlad on 12/1/22.
//

import SwiftUI
import shared

struct ExportBottomPanel: View {
    let colors: EditColors
    let dimens: EditDimens
    @State
    var isPlaying = true
    
    @ObservedObject
    var viewModel: ExportViewModel

    func progressViewUndetermined() -> some View {
        ZStack {
            ProgressView().progressViewStyle(CircularProgressViewStyle(tint: colors.exportProgressText.toSColor()))
            
        }.frame(height: CGFloat(dimens.exportBottomPanelHeightProgress))
            .exportBottomPanelShape(colors: colors, dimens: dimens)
    }

    
    var body: some View {
        VStack {
            if (DebugManager().isDebug) {
                HStack {
                    Text(String(format: "frameTime: %.2f",viewModel.debugRenderTimeCurrent))
                        .font(.system(size: 8))
                        .foregroundColor(.gray)
                        .padding(.leading, 10)
                    Spacer()
                    Text(String(format: "fullRenderTime: %.1f", viewModel.debugRenderTimeFull))
                        .font(.system(size: 8))
                        .foregroundColor(.gray)
                        .padding(.trailing, 10)
                }
            }
        Spacer()
            if let videoPreview = viewModel.videoPreview {
                LoopingPlayerView(url: videoPreview, isPlaying: $isPlaying)
                    .aspectRatio(viewModel.aspect.cg, contentMode: ContentMode.fit)
                .cornerRadius(15)
                .padding()
                .shadow(radius: 10)
                .onAppear {
                    isPlaying = true
                }
                .onDisappear {
                    isPlaying = false
                }
            }
            else if let preview = viewModel.preview {
            Image(uiImage: preview)
                .resizable()
                .aspectRatio(contentMode: .fit)
                .cornerRadius(15)
                .padding()
                .shadow(radius: 10)
            } else {
                ProgressView()
            }
        Spacer()
        ZStack {
            let state = viewModel.state

            if (state is ExportState.Initial) {
                ExportInitialPanelView(colors: colors, dimens: dimens, state: state as! ExportState.Initial, onChangeImageElseVideo: { imageElseVideo in
                    viewModel.onChangeImageElseVideo(imageElseVideo: imageElseVideo)
                }, onPickWhereToExport: { whereToExport in
                    viewModel.userPickedWhereToExport(whereToExport: whereToExport, fromDialog: false)
                }, onOpenDialogMore: {
                    viewModel.userPickedWhereToExport(whereToExport: WhereToExport(whereApp: "more", whereScreen: "more"), fromDialog: false)
                })

            } else if (state is ExportState.UserPicked) {

                progressViewUndetermined()

            } else if (state is ExportState.RenderingInProcess) {

                let s = state as! ExportState.RenderingInProcess

                if s.progress == nil {

                    progressViewUndetermined()
                } else {
                    ExportProgressView(progress: Float(truncating: s.progress!), colors: colors, dimens: dimens)
                }
            } else if (state is ExportState.Rendered) {

                ExportRenderedView(colors: colors, dimens: dimens, onPickWhereToExport: { whereToExport in
                    viewModel.userPickedWhereToExport(whereToExport: whereToExport, fromDialog: false)
                }, onOpenDialogMore: {
                    viewModel.userPickedWhereToExport(whereToExport: WhereToExport(whereApp: "more", whereScreen: "more"), fromDialog: false)
                }, onClickTagUs: {
                    
                    let commonViewModel: ExportCommonViewModel = Dependencies.resolveAuto()
                    commonViewModel.onClickInstInspiry(toString: {
                        res in res.localized()
                    })
                })
            }
            
        }.frame(maxWidth: .infinity)
            .padding(.top, 16)
        }
        .alert(isPresented: $viewModel.notAuthorizedMessage) {
            Alert(data: AlertData.getMediaAlert())
        }
    }

}

//struct ExportBottomPanel_Previews: PreviewProvider {
//    static var previews: some View {
//        ExportBottomPanel(colors: EditColorsLight(), dimens: EditDimensPhone())
//    }
//}
