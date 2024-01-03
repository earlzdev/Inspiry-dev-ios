//
//  AddPanel.swift
//  iosApp
//
//  Created by rst10h on 28.06.22.
//

import SwiftUI

struct AddPanel: View {
    @StateObject
    var model: AddInstrumentsModelApple
    @EnvironmentObject
    var editModel: EditViewModelApple
    
    var body: some View {
        VStack(spacing: 0) {
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
        .frame( height: model.dimens.barHeight.cg)
        .transition(.identity)
    }
}
