//
//  ExportRenderedView.swift
//  iosApp
//
//  Created by vlad on 8/11/21.
//

import SwiftUI
import shared

struct ExportRenderedView: View {
    let colors: EditColors
    let dimens: EditDimens
    let onPickWhereToExport: (WhereToExport) -> ()
    let onOpenDialogMore: () -> ()
    let onClickTagUs: () -> ()
    
    var body: some View {
        VStack(spacing: 12) {
            ExportSavedToGalleryView(colors: colors)
            
            VStack(spacing: 17) {
                
                ExportChoicesView(colors: colors, dimens: dimens, displayToGallery: true, onPick: onPickWhereToExport, onOpenDialogMore: onOpenDialogMore)
                
                ExportTagUsView(colors: colors, dimens: dimens, onClick: onClickTagUs)
                
            }.padding(.bottom, 15)
                .padding(.top, CGFloat(dimens.exportChoiceInnerPaddingTop))
                .exportBottomPanelShape(colors: colors, dimens: dimens)
        }
    }
}


struct ExportRenderedView_Previews: PreviewProvider {
    static var previews: some View {
        ExportRenderedView(colors: EditColorsLight(), dimens: EditDimensPhone(), onPickWhereToExport: { _ in }, onOpenDialogMore: {}, onClickTagUs: {})
    }
}
