//
//  ColorDilogItemPicker.swift
//  iosApp
//
//  Created by rst10h on 2.02.22.
//

import SwiftUI
import shared
import Cyborg

struct ColorItemPicker: View {
//    @State
    var selectedItem: Int
    let paletteItems: PaletteItems
    let layer: Int
    let colors: TextPaletteDialogColors
    let dimens: TextPaletteDialogDimens
    
    @State var color: SwiftUI.Color = 0.int32.ARGB
    
    let onItemSelected: (Int, SwiftUI.Color?) -> ()
    
    var body: some View {
        let items = paletteItems.getColorListForLayer(layer: layer.int32)
        let additionalElement = paletteItems.hasAdditionalColor(layer: layer.int32)
        let indices = paletteItems.getIndices(type: .color, size: items.count.int32)
        ScrollViewReader { sv in
            ScrollView(.horizontal, showsIndicators: false) {
                
                HStack {
                    ForEach(indices, id: \.self.intValue) { index in
                        let isSelected = selectedItem == index.intValue && index.intValue >= 0
                        switch index.int32Value {
                        case PaletteItems.companion.COLOR_PICKER_ITEM:
                            ColorPicker("Template color picker",
                                        selection: $color)
                                .labelsHidden()
                                .frame(
                                    width: dimens.itemSize.cg,
                                    height: dimens.itemSize.cg
                                )
                                .overlay(
                                    Circle()
                                        .fill(
                                            AngularGradient(gradient: Gradient(colors: ImageUtil.shared.sweepGradientIcon().map { $0.int32Value.ARGB }), center: .center)
                                        )
                                        .allowsHitTesting(false)
                                )

                        case PaletteItems.companion.REMOVE_ITEM:
                            CyborgImage(name: "ic_remove_color")
                                .frame(
                                    width: dimens.itemSize.cg,
                                    height: dimens.itemSize.cg
                                )
                                .onTapGesture {
                                    onItemSelected(PaletteItems.companion.REMOVE_ITEM.int, nil)
                                }
                        default:
                            let item = items[index.intValue]
                            let intColor = item.int32Value
                            
                            let color = intColor.ARGB
                            if (index != 0 || additionalElement) {
                                ColorItem(selected: isSelected,
                                          color: color,
                                          colors: colors,
                                          dimens: dimens)
                                    .onTapGesture {
                                        withAnimation {
                                            onItemSelected(index.intValue, nil)
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
                .onChange(of: color) { col in
                        onItemSelected(PaletteItems.companion.COLOR_PICKER_ITEM.int, col)
                    
                }
            }
        }
    }
}

struct ColorItemPicker_Previews: PreviewProvider {
    static var previews: some View {
        ColorItemPicker(
            selectedItem: 1,
            paletteItems: PaletteItems(colorsCount: 2),
            layer: 0,
            colors: TextColorDialogLightColors(),
            dimens: TextColorDialogDimensPhone()) { _, _ in }
            .preferredColorScheme(.dark)
    }
}
