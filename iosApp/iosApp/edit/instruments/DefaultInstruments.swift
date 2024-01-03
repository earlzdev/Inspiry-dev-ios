//
//  DefaultInstruments.swift
//  iosApp
//
//  Created by rst10h on 23.01.22.
//

import SwiftUI
import shared

struct DefaultInstrumentsUI: View {
    @StateObject
    var model: DefaultInstrumentsModelApple
    @EnvironmentObject
    var editModel: EditViewModelApple
    
    var body: some View {
        ZStack(alignment: .center) {
            GeometryReader { geo in
                ScrollView(.horizontal) {
                    HStack(alignment: .center, spacing: 0) {
                        ForEach(model.menu.getKeys(), id:\.self) { item in
                            let menuItem = model.menu.getMenuItem(item: item)
                            let color = model.isHighlighted(item) ? model.colors.activeTextColor : model.colors.inactiveTextColor
                            Button(action: {
                                withAnimation {
                                    model.select(item)
                                }
                            }) {
                                InstrumentMenuItem(
                                    width: model.dimens.instrumentsIconSize.cg,
                                    height: model.dimens.instrumentsIconSize.cg,
                                    icon: menuItem.icon ?? "",
                                    tabName: menuItem.text.localized(),
                                    color: color.toSColor()
                                )
                            }
                            .frame(width: model.dimens.instrumentItemWidth.cg)
                        }
                    }
                    .frame(width: geo.size.width)
                }
                .background(model.colors.background.toSColor())
            }
        }
        .frame(height: model.dimens.barHeight.cg)
        .transition(.identity)
    }
}

struct DefaultInstruments_Previews: PreviewProvider {
    
    static func fakeMenu() -> CommonMenu<DefaultInstruments> {
        let menu = CommonMenu<DefaultInstruments>(iconSize: 20)
        menu.setMenuItem(
            item: .defaultAdd,
            text: MR.strings().instrument_text,
            icon: "ic_instrument_animation"
        )
        menu.setMenuItem(
            item: .defaultColor,
            text: MR.strings().instrument_text_color,
            icon: "ic_default_time"
        )
        
        return menu
    }
    
    static let fakeTemplateView = InspTemplateViewApple.fakeInitializedTemplate()
    static let model = DefaultInstrumentsModelApple(
        model: DefaultInstrumentsPanelViewModel(
            templateView: fakeTemplateView,
            analyticsManager: Dependencies.resolveAuto(),
            licenseManager: Dependencies.resolveAuto(),
            json: Dependencies.resolveAuto(),
            menu: fakeMenu()))
    
    static var previews: some View {
        DefaultInstrumentsUI(model: model)
            .environmentObject(EditViewModelApple.modelForPreviews())
    }
    
}
