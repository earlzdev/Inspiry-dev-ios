//
//  OpacityGroup.swift
//  iosApp
//
//  Created by rst10h on 4.02.22.
//

import SwiftUI

struct OpacityGroup: View {
    @EnvironmentObject
    var model: ColorDialogModelApple
    
    var body: some View {
        
        VStack(spacing: 0) {
            Spacer()
                .frame(height: model.dimens.itemsSpacer.cg)
            ForEach(0..<model.getAlphaLayersCount(), id: \.self) { layer in
                SimpleInspSlider(progress: model.getCurrentAlphaForLayer(layer)) { value in
                    model.onAlphaChanged(layer: layer, alpha: value)
                }
                .frame(width: UIScreen.screenWidth * 0.75, height: model.dimens.itemListHeight.cg, alignment: .center)
                .padding(.leading, model.dimens.panelStartPadding.cg)
                Spacer()
                    .frame(height: model.dimens.itemsSpacer.cg)
            }
        }
        
    }
}

struct OpacityGroup_Previews: PreviewProvider {
    static var previews: some View {
        OpacityGroup()
            .environmentObject(ColorDialogModelApple(InspTemplateViewApple.fakeInitializedTemplate()){})
            .preferredColorScheme(.dark)
    }
}
