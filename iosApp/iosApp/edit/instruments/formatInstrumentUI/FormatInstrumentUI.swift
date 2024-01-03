//
//  FormatInstrumentUI.swift
//  iosApp
//
//  Created by rst10h on 6.02.22.
//

import SwiftUI
import shared

struct FormatInstrumentUI: View {
    
    @StateObject
    var model: FormatInstrumentModelApple
    @EnvironmentObject
    private var licenseManagerWrapper: LicenseManagerAppleWrapper
    
    let colors = FormatInstrumentColorsLight()
    let dimens = FormatInstrumentDimensPhone()
    
    var body: some View {
        HStack {
            let formats = FormatsProviderImpl().getFormats()
            ForEach(formats, id: \.templateFormat) { format in
                
                VStack {
                    let isSelected = model.currentFormat == format.templateFormat
                    let color = isSelected ? colors.activeTextColor.toSColor() : colors.inactiveTextColor.toSColor()
                    FormatIcon(
                        colors: colors,
                        dimens: dimens,
                        format: format,
                        color: color,
                        hasPremium: licenseManagerWrapper.hasPremium)
                    Text(format.text.localized())
                        .font(.system(size: dimens.labelTextSize.cg))
                        .lineLimit(1)
                        .foregroundColor(model.currentFormat == format.templateFormat ? colors.activeTextColor.toSColor() : colors.inactiveTextColor.toSColor())
                    
                }
                .frame(width: dimens.instrumentItemWidth.cg, height: dimens.barHeight.cg, alignment: .center)
                .onTapGesture {
                    withAnimation {
                        model.selectFormat(format, hasPremium: licenseManagerWrapper.hasPremium)
                        print("current format \(model.currentFormat)")
                    }
                }
                .onLongPressGesture {
                    if (DebugManager().isDebug) {
                        model.selectFormat(format, hasPremium: true)
                    }
                }
            }
        }
        .frame(maxWidth: .infinity)
        .background(colors.background.toSColor())
    }
}

struct FormatInstrumentUI_Previews: PreviewProvider {
    @State
    static var format = TemplateFormat.story
    static let tv = InspTemplateViewApple.fakeInitializedTemplate()
    static let model = FormatInstrumentModelApple(model: FormatSelectorViewModel(templateView: tv, licenseManager: Dependencies.resolveAuto()){ _ in})
    static var previews: some View {
        FormatInstrumentUI(model: model)
            .preferredColorScheme(.dark)
    }
}
