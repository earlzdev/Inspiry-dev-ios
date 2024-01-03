//
//  ShapesPanel.swift
//  iosApp
//
//  Created by rst10h on 4.07.22.
//

import SwiftUI
import shared

struct ShapesPanel: View {
    @StateObject
    var model: ShapeInstrumentsModelApple
    
    var body: some View {
        VStack(spacing: 0) {
            Spacer(minLength: 5)
            GeometryReader { geo in
                ScrollView(.horizontal) {
                    HStack(alignment: .center, spacing: 0) {
                        ForEach(model.shapes, id:\.self) { item in
                            let color = (item == model.activeShape ?? .nothing) ? 0xffababab.ARGB : 0xff363636.ARGB
                            Button(action: {
                                withAnimation {
                                    model.select(item)
                                }
                            }) {
                                CyborgImage(name: model.coreModel.getIcon(shape: item))
                                    .frame(width: 30, height: 30)
                                    .padding(.horizontal, 10)
                                    .padding(.vertical, 10)
                                    .background(RoundedRectangle(cornerRadius: 5).fill(color))
                            }
                            .frame(width: model.dimens.instrumentItemWidth.cg)
                        }
                    }
                    
                }
                .frame(width: geo.size.width)
                .background(model.colors.background.toSColor())
            }
        }
        .frame( height: model.dimens.barHeight.cg)
        .transition(.identity)
    }
}
