//
//  PaletteItemGroup.swift
//  iosApp
//
//  Created by rst10h on 3.02.22.
//

import SwiftUI

struct PaletteItemGroup: View {
    @EnvironmentObject
    var model: ColorDialogModelApple
    
    var body: some View {
        VStack(spacing: 0) {
            Spacer()
                .frame(height: model.dimens.itemsSpacer.cg)
            ForEach(0 ..< model.paletteLayerCount, id: \.self) { layer in
                PaletteItemPicker(
                    selectedItem: model.getCurrentPaletteForLayer(layer),
                    paletteItems: model.paletteItems(),
                    layer: layer,
                    colors: model.colors,
                    dimens: model.dimens) { itemClicked in
                        model.onPalettePick(id: itemClicked)
                    }
                    .frame(height: model.dimens.itemListHeight.cg, alignment: .center)                
                Spacer()
                    .frame(height: model.dimens.itemsSpacer.cg)
            }
        }
    }
}

struct PaletteItemGroup_Previews: PreviewProvider {
    static var previews: some View {
        PaletteItemGroup()
            .environmentObject(ColorDialogModelApple(InspTemplateViewApple.fakeInitializedTemplate()) {})
            .preferredColorScheme(.dark)
    }
}
