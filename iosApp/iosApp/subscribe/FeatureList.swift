//
//  FeatureList.swift
//  iosApp
//
//  Created by rst10h on 6.12.22.
//

import Foundation
import SwiftUI
import shared

struct FeatureList: View {
    let colors: SubscribeColors
    let dimens: SubscribeDimens
    @StateObject
    var model = FeatureListViewModel()
    
    var scroll: some Gesture {
        DragGesture(coordinateSpace: .global)
            .onChanged { point in
                model.onUserScroll(delta: point.translation.width)
            }
            .onEnded { _ in
                model.onScrollEnded()
            }
    }
    
    var body: some View {
        ZStack() {
            GeometryReader { geo in
                let fullItemWidth = dimens.featuresListItemStartEndPadding.cg * 2 + dimens.featuresListItemHeight.cg
                ForEach(model.items.indices, id: \.self) { index in
                    let featureIndex = model.items[index]
                    let feature = model.featuresList[featureIndex]
                    FeatureView(dimens: dimens, feature: feature)
                        .offset(x: model.getOffsetForItem(item: index))
                }
                .onAppear {
                    model.startScrolling(itemWidth: fullItemWidth, frameWidth: geo.size.width)
                }
            }
            .gesture(scroll)
            
        }
        .frame(
            minWidth: 0,
            maxWidth: .infinity,
            alignment: .topLeading
        )
        .frame(height: (dimens.featuresListTopPadding.cg + dimens.featuresListBottomPadding.cg + dimens.featuresListItemHeight.cg))
    }
}

struct FeatureView: View {
    let dimens: SubscribeDimens
    let feature: SubscribeFeature
    var body: some View {
        ZStack(alignment: .bottomLeading) {
            Image(uiImage: feature.image)
                .resizable()
            Text(feature.text.localized())
                .foregroundColor(Color.white)
                .font(.system(size: 13))
                .bold()
                .shadow(radius: 5.cg)
                .padding(10.cg)
        }
        .frame(width: dimens.featuresListItemHeight.cg, height: dimens.featuresListItemHeight.cg, alignment: .bottomLeading)
        .padding(.horizontal, dimens.featuresListItemStartEndPadding.cg)
        .padding(.top, dimens.featuresListTopPadding.cg)
        .padding(.bottom, dimens.featuresListBottomPadding.cg)
    }
}
