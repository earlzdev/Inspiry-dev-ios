//
//  ColorItemsGroup.swift
//  iosApp
//
//  Created by rst10h on 3.02.22.
//

import SwiftUI

struct ColorItemsGroup: View {
    @EnvironmentObject
    var model: ColorDialogModelApple
    
    var body: some View {
        VStack(spacing: 0) {
            Spacer()
                .frame(height: model.dimens.itemsSpacer.cg)
            ForEach(0 ..< model.colorLayerCount, id: \.self) { layer in
                ColorItemPicker(
                    selectedItem: model.getCurrentColorForLayer(layer),
                    paletteItems: model.paletteItems(),
                    layer: layer,
                    colors: model.colors,
                    dimens: model.dimens) { itemClicked, color in
                        if (color != nil) {
                            model.onPickCOlor(layer: layer, color: color!)
                        } else {
                        model.onColorPick(layer: layer, id: itemClicked)
                        }
                    }
                    .frame(height: model.dimens.itemListHeight.cg, alignment: .center)
                Spacer()
                    .frame(height: model.dimens.itemsSpacer.cg)
            }
        }
    }
}

struct ColorItemsGroup_Previews: PreviewProvider {
    static var previews: some View {
        ColorItemsGroup()
            .environmentObject(ColorDialogModelApple(InspTemplateViewApple.fakeInitializedTemplate()){})
            .preferredColorScheme(.dark)
    }
}
