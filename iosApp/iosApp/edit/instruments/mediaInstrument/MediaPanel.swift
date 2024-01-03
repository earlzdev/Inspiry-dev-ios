//
//  MediaPanel.swift
//  iosApp
//
//  Created by rst10h on 2.07.22.
//

import SwiftUI

struct MediaPanel: View {
    @StateObject
    var model: MediaInstrumentsModelApple
    
    var body: some View {
        VStack(spacing: 0) {
            GeometryReader { geo in
                ScrollView(.horizontal) {
                    HStack(alignment: .center, spacing: 0) {
                        ForEach(model.menu.getKeys(), id:\.self) { item in
                            let menuItem = model.menu.getMenuItem(item: item)
                            let color = (item == model.activeInstrument || model.activeInstrument == nil) ? model.colors.activeTextColor : model.colors.inactiveTextColor
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
        .frame( height: model.dimens.barHeight.cg)
        .transition(.identity)
    }
}
