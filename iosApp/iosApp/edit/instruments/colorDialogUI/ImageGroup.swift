//
//  ImageGroup.swift
//  iosApp
//
//  Created by rst10h on 4.02.22.
//

import SwiftUI
import shared
import Kingfisher
import PhotosUI

struct ImageGroup: View {
    @EnvironmentObject
    var model: ColorDialogModelApple
    
    @State var pickerVisible = false
    @State var isLoading = false
    @State var loadingProgress: Double? = nil
    
    var body: some View {
        HStack {
            VStack(spacing: 0) {
                Button( action: {
                    PHPhotoLibrary.requestAuthorization(for: .readWrite) { status in
                        switch status {
                        case .authorized:
                            pickerVisible.toggle()
                        case .limited:
                            pickerVisible.toggle()
                        @unknown default:
                            break
                        }
                    }
                }) {
                    KFImage(URL(string: model.mediaResult.first?.uri ?? ""))
                        .placeholder {
                            BackgroundPlaceholder(colors: model.colors, dimens: model.dimens)
                            
                        }
                        .resizable()
                        .aspectRatio(SharedConstants.shared.ASPECT_10by16.cg, contentMode: .fit)
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                }
            }
            .frame(height: 130)
            .padding(.horizontal)
            Spacer()
        }
        .frame(maxWidth: .infinity)
        .sheet(isPresented: $pickerVisible) {
            //ImagePickerUI(mediaResult: $model.mediaResult, isActive: $pickerVisible)
            MediaPickerUI(mediaResult: $model.mediaResult, isActive: $pickerVisible, isLoading: $isLoading, iCloudProgress: $loadingProgress, maxMediasCount: 1)
        }
        
        
    }
    
}

struct BackgroundPlaceholder: View {
    let colors: TextPaletteDialogColors
    let dimens: TextPaletteDialogDimens
    var body: some View {
        VStack(spacing: 10) {
            CyborgImage(name: "ic_add_icon")
                .frame(
                    width: dimens.itemSize.cg * 0.7,
                    height: dimens.itemSize.cg * 0.7
                )
            Text(MR.strings().palette_add_image.localized())
                .font(.system(size: 12))
                .foregroundColor(colors.addFromGallery.toSColor())
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .aspectRatio(SharedConstants.shared.ASPECT_10by16.cg, contentMode: .fit)
        .background(colors.placeholderBackground.toSColor())
    }
}

struct ImageGroup_Previews: PreviewProvider {
    static var previews: some View {
        ImageGroup()
            .environmentObject(ColorDialogModelApple(InspTemplateViewApple.fakeInitializedTemplate()){})
            .preferredColorScheme(.dark)
    }
}
