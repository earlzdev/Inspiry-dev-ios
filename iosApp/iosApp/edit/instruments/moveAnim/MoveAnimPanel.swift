//
//  MoveAnimPanel.swift
//  iosApp
//
//  Created by rst10h on 13.01.23.
//

import SwiftUI
import shared

struct MoveAnimPanel: View {
    @StateObject
    var model: MoveAnimInstrumentModelApple
    @EnvironmentObject
    var editModel: EditViewModelApple
    
    var body: some View {
        VStack(spacing: 0) {
            Spacer(minLength: 5)
            GeometryReader { geo in
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(alignment: .center, spacing: 0) {
                        ForEach(model.moveAnims, id:\.self) { item in
                            let color = (item == model.activeAnim ?? .none) ? 0xffababab.ARGB : 0xff363636.ARGB
                            Button(action: {
                                withAnimation {
                                    model.wrapperHelper = editModel.wrapperHelper
                                    model.select(item)
                                }
                            }) {
                                CyborgImage(name: model.coreModel.getIcon(anim: item))
                                    .frame(width: 30, height: 30)
                                    .padding(.horizontal, 6)
                                    .padding(.vertical, 6)
                                    .background(RoundedRectangle(cornerRadius: 5).fill(color))
                            }
                            .frame(width: 55.cg)
                        }
                    }
                    .padding(.top, 6)
                }
                .frame(width: geo.size.width)
                .background(model.colors.background.toSColor())
            }
        }
        .frame( height: model.dimens.barHeight.cg)
        .transition(.identity)
    }
}
