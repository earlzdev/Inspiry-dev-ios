//
// Created by vlad on 9/12/21.
//

import SwiftUI
import shared

struct ExportChoicesView: View {
    let colors: EditColors
    let dimens: EditDimens
    let displayToGallery: Bool
    let onPick: (WhereToExport) -> ()
    let onOpenDialogMore: () -> ()

    @State(initialValue: getPredefinedExportApps())
    var predefinedApps: [PredefinedExportApp]

    func item(text: String, onClick: @escaping () -> (), image: () -> AnyView) -> some View {

        Button(action: onClick) {
            VStack {
                ZStack {

                    image()

                }.frame(width: CGFloat(dimens.exportChoiceImageSize), height: CGFloat(dimens.exportChoiceImageSize))
                        .padding(.bottom, CGFloat(dimens.exportChoiceImagePaddingBottom))
                        .padding(.top, CGFloat(dimens.exportChoiceImagePaddingTop))
                        .padding(.horizontal, CGFloat(dimens.exportChoiceImagePaddingHorizontal))

                Text(text)
                        .font(.system(size: CGFloat(dimens.exportChoiceTextSize), weight: .bold))
                        .lineLimit(2)
                        .foregroundColor(colors.exportToAppText.toSColor())
                        .frame(maxWidth: CGFloat(dimens.exportChoiceTextMaxWidth))
                        .padding(.horizontal, CGFloat(dimens.exportChoiceTextPaddingHorizontal))

            }.frame(maxHeight: .infinity)

        }.frame(maxHeight: .infinity)
    }

    var body: some View {
        HStack(spacing: 0) {

            ForEach(predefinedApps, id: \.whereToExport.whereApp) { app in

                item(text: app.name, onClick: { onPick(app.whereToExport) }, image: {
                    
                    let view = Image(app.iconRes)
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                    return AnyView(view)
                })

            }

            if displayToGallery {
                item(text: MR.strings().export_option_gallery.localized(),
                        onClick: { onPick(WhereToExport(whereApp: "Gallery")) }, image: {

                    let view = CyborgImage(name: "ic_export_gallery")
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                    return AnyView(view)
                })
            }

            item(text: MR.strings().export_option_more.localized(),
                    onClick: onOpenDialogMore, image: {

                let view = CyborgImage(name: "ic_export_more")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                return AnyView(view)
            })

        }.frame(maxWidth: .infinity)
                .frame(height: CGFloat(dimens.exportChoiceSingleHeight))
    }
}

struct ExportChoicesView_Previews: PreviewProvider {
    static var previews: some View {
        ExportChoicesView(colors: EditColorsLight(), dimens: EditDimensPhone(), displayToGallery: true, onPick: { _ in }, onOpenDialogMore: {})
    }
}
