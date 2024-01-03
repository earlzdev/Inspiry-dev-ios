//
//  FontsListViewWithProgress.swift
//  iosApp
//
//  Created by vlad on 14/7/21.
//

import SwiftUI
import shared

struct FontsListViewWithProgress: View {
    let colors: FontDialogColors
    let dimens: FontDialogDimens
    let fonts: InspResponse<FontPathsResponse>
    let isUpload: Bool
    let selectedFont: FontPath
    let onSelectedChanged: (FontPath) -> ()
    
    
    var body: some View {
        
        if fonts is InspResponseLoading {
            ZStack {
                ProgressView().accentColor(colors.fontTextActive.toSColor())
                
            }
            .frame(height: CGFloat(dimens.fontsListHeight), alignment: .center)
            .frame(maxWidth: .infinity)
        }
        else if fonts is InspResponseError {
            ZStack {
                Text("Can't load fonts")
            }
            .frame(height: CGFloat(dimens.fontsListHeight), alignment: .center)
            .frame(maxWidth: .infinity)
        } else if fonts is InspResponseData {
            
            FontsListView(colors: colors, dimens: dimens, fonts: (fonts as! InspResponseData).data!.fonts, isUpload: isUpload, selectedFont: selectedFont, onSelectedChanged: onSelectedChanged)
        }
    }
}

struct FontsListViewWithProgress_Previews: PreviewProvider {
    static var previews: some View {
        let defaultFont = Dependencies.diContainer.resolve(PlatformFontPathProvider.self)!.defaultFont()
        let fontsManager = Dependencies.diContainer.resolve(FontsManager.self)!
        
        FontsListViewWithProgress(colors: FontDialogColorsDark(), dimens: FontDialogDimensPhone(),
                                  fonts: InspResponseData(data: FontPathsResponse(fonts: fontsManager.allPredefinedFonts)), isUpload: true, selectedFont: defaultFont, onSelectedChanged: { _ in })
    }
}
