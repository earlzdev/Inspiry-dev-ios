//
//  PaletteItem.swift
//  iosApp
//
//  Created by rst10h on 3.02.22.
//

import SwiftUI
import shared

struct PaletteItem: View {
    let selected: Bool
    let palette: KotlinIntArray
    let colors: TextPaletteDialogColors
    let dimens: TextPaletteDialogDimens
    let isSelected: Bool = false
    var body: some View {
        if (!selected) {
            VStack(spacing: 0) {
                ForEach(0..<palette.size.int, id: \.self) { index in
                    let color = palette.get(index: index.int32).ARGB
                    Rectangle()
                        .fill(color)
                        .frame(maxWidth: .infinity)
                }
            }
            .frame(
                width: dimens.itemSize.cg,
                height: dimens.itemSize.cg
            )
            .clipShape(RoundedRectangle(cornerRadius: dimens.paletteItemCornerRadius.cg))
            .overlay(
                RoundedRectangle(cornerRadius: dimens.paletteItemCornerRadius.cg)
                    .stroke(colors.paletteBorderColor.toSColor(), lineWidth: dimens.paletteBorderWidth.cg)
            )
            
            
            
        } else {
            ZStack {
                VStack(spacing: 0) {
                    ForEach(0..<palette.size.int, id: \.self) { index in
                        let color = palette.get(index: index.int32).ARGB
                        Rectangle()
                            .fill(color)
                            .frame(maxWidth: .infinity)
                    }
                }
                .clipShape(RoundedRectangle(cornerRadius: dimens.paletteItemCornerRadius.cg))
                .overlay(
                    RoundedRectangle(cornerRadius: dimens.paletteItemCornerRadius.cg)
                        .stroke(colors.selectedItemInnerBorder.toSColor(), lineWidth: 2)
                )
                .frame(
                    width: dimens.itemSize.cg * 0.95,
                    height: dimens.itemSize.cg * 0.95
                )
                
            }
            .frame(
                width: dimens.outerBorderActiveItem.cg,
                height: dimens.outerBorderActiveItem.cg,
                alignment: .center
            )
            .background(
                RoundedRectangle(cornerRadius: dimens.paletteItemCornerRadius.cg * 1.2)
                    .fill(colors.selectedItemOuterBorder.toSColor()))
        }
        
        
        
    }
}

struct PaletteItem_Previews: PreviewProvider {
    static var previews: some View {
        PaletteItem(
            selected: false,
            palette: PaletteProviderImpl().getThreeColors()[4],
            colors: TextColorDialogLightColors(),
            dimens: TextColorDialogDimensPhone()
        )
            .preferredColorScheme(.dark)
    }
}
