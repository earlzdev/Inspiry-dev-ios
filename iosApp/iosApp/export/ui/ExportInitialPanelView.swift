//
//  ExportUserChoiceView.swift
//  iosApp
//
//  Created by vlad on 8/11/21.
//

import SwiftUI
import shared

struct ExportInitialPanelView: View {
    let colors: EditColors
    let dimens: EditDimens

    let state: ExportState.Initial
    let onChangeImageElseVideo: (Bool) -> ()
    let onPickWhereToExport: (WhereToExport) -> ()
    let onOpenDialogMore: () -> ()

    var body: some View {

        VStack(spacing: 0) {

            ExportImageElseVideoView(colors: colors, dimens: dimens,
                    imageElseVideo: state.imageElseVideo, onChange: onChangeImageElseVideo)

            ZStack {

                ExportChoicesView(colors: colors, dimens: dimens, displayToGallery: true, onPick: onPickWhereToExport, onOpenDialogMore: onOpenDialogMore)

            }.padding(.top, CGFloat(dimens.exportChoiceInnerPaddingTop))
                    .padding(.bottom, CGFloat(dimens.exportChoiceInnerPaddingBottom))
                    .exportBottomPanelShape(colors: colors, dimens: dimens)
                    .padding(.top, CGFloat(dimens.exportChoicePaddingTop))

        }.frame(maxWidth: .infinity, alignment: .top)
    }
}

struct ExportUserChoiceView_Previews: PreviewProvider {
    static var previews: some View {
        let colors = EditColorsLight()
        let dimens = EditDimensPhone()
        ZStack {
            ExportInitialPanelView(colors: colors, dimens: dimens, state: ExportState.Initial(imageElseVideo: false), onChangeImageElseVideo: { it in }, onPickWhereToExport: { export in }, onOpenDialogMore: {})

        }.frame(maxWidth: .infinity)
                .background(colors.instrumentsBar.toSColor())
    }
}
