//
//  GradientItem.swift
//  iosApp
//
//  Created by rst10h on 3.02.22.
//

import SwiftUI
import shared

struct GradientItem: View {
    let selected: Bool
    let gradient: PaletteLinearGradient
    let colors: TextPaletteDialogColors
    let dimens: TextPaletteDialogDimens
    let isSelected: Bool = false
    var body: some View {
        if (!selected) {
            Circle()
                .fill(LinearGradient.fromPaletteLinearGradient(gradient))
                .frame(
                    width: dimens.itemSize.cg,
                    height: dimens.itemSize.cg
                )
        } else {
            ZStack {
                Circle()
                    .fill(LinearGradient.fromPaletteLinearGradient(gradient))
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

struct GradientItem_Previews: PreviewProvider {
    static let gradients: [PaletteLinearGradient] = PaletteProviderImpl().getGradients() as! [PaletteLinearGradient]
    static var previews: some View {
        GradientItem(
            selected: true,
            gradient: gradients[5],
            colors: TextColorDialogLightColors(),
            dimens: TextColorDialogDimensPhone()
        )
            .preferredColorScheme(.dark)
    }
}
