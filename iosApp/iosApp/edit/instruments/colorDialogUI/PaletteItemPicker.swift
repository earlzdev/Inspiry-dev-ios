//
//  PaletteItemPicker.swift
//  iosApp
//
//  Created by rst10h on 3.02.22.
//

import SwiftUI
import shared

struct PaletteItemPicker: View {
    //@State
    var selectedItem: Int
    let paletteItems: PaletteItems
    let layer: Int
    let colors: TextPaletteDialogColors
    let dimens: TextPaletteDialogDimens
    
    let onItemSelected: (Int) -> ()
    
    var body: some View {
        let items: [KotlinIntArray] = paletteItems.getPaletteList()
        let additionalElement: Bool = paletteItems.hasAdditionalGradient(layer: layer.int32)
        let indices = paletteItems.getIndices(type: .gradient, size: items.count.int32)
        ScrollViewReader { sv in
            ScrollView(.horizontal, showsIndicators: false) {
                HStack {
                    ForEach(indices, id: \.self.intValue) { index in
                        let isSelected = selectedItem == index.intValue && index.intValue >= 0
                        switch index.int32Value {
                        case PaletteItems.companion.REMOVE_ITEM:
                            CyborgImage(name: "ic_remove_color")
                                .frame(
                                    width: dimens.itemSize.cg,
                                    height: dimens.itemSize.cg
                                )
                                .onTapGesture {
                                    withAnimation {
                                        onItemSelected(PaletteItems.companion.REMOVE_ITEM.int)
                                    }
                                }
                        default:
                            let palette = items[index.intValue]
                            if (index != 0 || additionalElement) {
                                PaletteItem(selected: isSelected,
                                            palette: palette,
                                            colors: colors,
                                            dimens: dimens)
                                    .onTapGesture {
                                        withAnimation {
                                            onItemSelected(index.intValue)
                                            //selectedItem = index.intValue
                                            sv.scrollTo(index.intValue, anchor: .center)
                                        }
                                    }
                            }
                        }
                    }
                }
                .padding(.leading, dimens.panelStartPadding.cg)
                .padding(.trailing, dimens.panelEndPadding.cg)
                .onAppear() {
                    withAnimation {
                        sv.scrollTo(selectedItem, anchor: .center)
                    }
                }
            }
        }
    }
}

struct PaletteItemPicker_Previews: PreviewProvider {
    static var previews: some View {
        PaletteItemPicker(
            selectedItem: 1,
            paletteItems: PaletteItems(colorsCount: 2),
            layer: 0,
            colors: TextColorDialogLightColors(),
            dimens: TextColorDialogDimensPhone()) { _ in }
            .preferredColorScheme(.dark)
    }
}
