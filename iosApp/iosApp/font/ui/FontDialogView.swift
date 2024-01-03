//
//  FontDialogView.swift
//  iosApp
//
//  Created by vlad on 13/7/21.
//

import SwiftUI
import shared

struct FontDialogView: View {
    
    private let colors: FontDialogColors = FontDialogColorsDark()
    private let dimens: FontDialogDimens = FontDialogDimensPhone()
    
    private let fontsManager: FontsManager = Dependencies.diContainer.resolve(FontsManager.self)!
    
    private let uploadedFontsProvider: UploadedFontsProvider = Dependencies.diContainer.resolve(UploadedFontsProvider.self)!
    
    private let analyticsManager: AnalyticsManager = Dependencies.diContainer.resolve(AnalyticsManager.self)!
    
    private let textCaseHelper: TextCaseHelper = Dependencies.diContainer.resolve(TextCaseHelper.self)!
    
    private let plaformFontPathProvider: PlatformFontPathProvider = Dependencies.diContainer.resolve(PlatformFontPathProvider.self)!
    

    @StateObject
    var viewModel: FontsViewModelApple
    
    var body: some View {
        VStack(spacing: 0) {
            
            FontStylesView(colors: colors, dimens: dimens,
                           font: viewModel.currentFontPath,
                           text: viewModel.currentText,
                           selectedStyle: viewModel.currentFontStyle, platformFontPathProvider: plaformFontPathProvider, textCaseHelper: textCaseHelper,
                           onSelectedChange: { it in
                viewModel.coreModel.onFontStyleChange(style: it)
                viewModel.updateLayout()

            }, onToggleCapsMode: { viewModel.coreModel.onClickToggleCapsMode() })
            
            FontCategoriesView(colors: colors, dimens: dimens, categories: fontsManager.allCategories, selectedCategoryIndex: viewModel.currentCategoryIndex, onSelectedChange: { it in
                
                viewModel.coreModel.currentCategoryIndex.setValue(it)
            })
            
            let fonts = viewModel.currentFonts
            
            let isUpload = fontsManager.allCategories[viewModel.currentCategoryIndex] == FontsManager.Companion().CATEGORY_ID_UPLOAD
            
            FontsListViewWithProgress(colors: colors, dimens: dimens, fonts: fonts, isUpload: isUpload, selectedFont: viewModel.currentFontPath, onSelectedChanged: { item in
                if item.forPremium { //todo check premium state
                    viewModel.coreModel.onSubscribe()
                } else {
                    viewModel.coreModel.onPickedNewFont(path:item)
                    viewModel.updateLayout()
                }
            })
            
        }
        .frame(height: CGFloat(dimens.panelHeight), alignment: .top)
        .frame(maxWidth: .infinity)
        .background(colors.backgroundColor.toSColor())
        
    }
}

//struct FontDialogView_Previews: PreviewProvider {
//    static var previews: some View {
//        FontDialogView(fontData: nil, text: "Text", callbacks: EmptyFontDialogCallbacks())
//    }
//}
