//
//  ColorItem.swift
//  iosApp
//
//  Created by rst10h on 2.02.22.
//

import SwiftUI
import shared

struct ColorItem: View {
    let selected: Bool
    let color: SwiftUI.Color
    let colors: TextPaletteDialogColors
    let dimens: TextPaletteDialogDimens
    let isSelected: Bool = false
    var body: some View {
        if (!selected) {
            Circle()
                .fill(color)
                .frame(
                    width: dimens.itemSize.cg,
                    height: dimens.itemSize.cg
                )
        } else {
            ZStack {
                Circle()
                    .fill(color)
                    .overlay(
                        Circle()
                            .stroke(colors.selectedItemInnerBorder.toSColor(), lineWidth: dimens.activeItemBorderWidth.cg)
                            .padding(2)
                        
                    )
                    .frame(
                        width: dimens.itemSize.cg,
                        height: dimens.itemSize.cg
                    )
                
            }
            .frame(
                width: dimens.outerBorderActiveItem.cg,
                height: dimens.outerBorderActiveItem.cg,
                alignment: .center
            )
            .background(Circle().fill(colors.selectedItemOuterBorder.toSColor()))
        }
        
        
        
    }
}

struct ColorItem_Previews: PreviewProvider {
    static var previews: some View {
        ColorItem(
            selected: true,
            color: .blue,
            colors: TextColorDialogLightColors(),
            dimens: TextColorDialogDimensPhone()
        )
            .preferredColorScheme(.dark)
    }
}
