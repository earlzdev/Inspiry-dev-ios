//
//  GradientItemsGroup.swift
//  iosApp
//
//  Created by rst10h on 3.02.22.
//

import SwiftUI

import SwiftUI

struct GradientItemsGroup: View {
    @EnvironmentObject
    var model: ColorDialogModelApple
    
    var body: some View {
        VStack(spacing: 0) {
            Spacer()
                .frame(height: model.dimens.itemsSpacer.cg)
            
            ForEach(0 ..< model.gradientLayerCount, id: \.self) { layer in
                GradientItemPicker(
                    selectedItem: model.getCurrentGradientForLayer(layer),
                    paletteItems: model.paletteItems(),
                    layer: layer,
                    colors: model.colors,
                    dimens: model.dimens) { itemClicked in
                        model.onGradientPick(layer: layer, id: itemClicked)
                    }
                    .frame(height: model.dimens.itemListHeight.cg, alignment: .center)
                Spacer()
                    .frame(height: model.dimens.itemsSpacer.cg)
            }
            
            ForEach(model.gradientLayerCount ..< model.colorLayerCount, id: \.self) {layer in
                ColorItemPicker(
                    selectedItem: model.getCurrentColorForLayer(layer),
                    paletteItems: model.paletteItems(),
                    layer: layer,
                    colors: model.colors,
                    dimens: model.dimens) { itemClicked, _ in
                        model.onColorPick(layer: layer, id: itemClicked)
                    }
                    .frame(height: model.dimens.itemListHeight.cg, alignment: .center)
                Spacer()
                    .frame(height: model.dimens.itemsSpacer.cg)
            }
        }
    }
}

struct GradientItemsGroup_Previews: PreviewProvider {
    static var previews: some View {
        GradientItemsGroup()
            .environmentObject(ColorDialogModelApple(InspTemplateViewApple.fakeInitializedTemplate()){})
            .preferredColorScheme(.dark)
    }
}
