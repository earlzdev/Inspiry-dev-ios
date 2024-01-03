//
//  TimeLineSliderUI.swift
//  iosApp
//
//  Created by rst10h on 25.08.22.
//

import SwiftUI
import shared

public struct TimeLineSliderUI: View {
    
    @ObservedObject
    var model: TrimVideoModel
    
    private let dragHelper = DragGestureHelper()
   
    public var body: some View {
        GeometryReader { geo in
            let selectedWidth = geo.size.width * (model.maxValue.cg - model.minValue.cg)
            let innerWidth = selectedWidth - 30
            ZStack (alignment: Alignment(horizontal: .leading, vertical: .center)) {
                
                
                ThumbnailsViewRelresentable(trimModel: model)
                    .cornerRadius(11)
                    .frame(height: 50)
                    .overlay(
                        RoundedRectangle(cornerRadius: 10).strokeBorder(Color.black, lineWidth: 3)
                    )
                ZStack {
                    
                }
                .frame(width: selectedWidth, height: 60, alignment: .center)
                .background(Color.white)
                .cornerRadius(10)
                .overlay(HStack {
                    CyborgImage(name: "ic_arrow_trim_left")
                        .frame(width: 8, height: 15)
                        .padding(.leading, 2.5)
                    Spacer()
                    CyborgImage(name: "ic_arrow_trim_right")
                        .frame(width: 8, height: 15)
                        .padding(.trailing, 2.5)
                })
                .mask(
                    ZStack {
                        Color.white
                        Rectangle()
                            .fill(Color.black)
                            .frame(width: innerWidth > 0 ? innerWidth : 0, height: 50) //todo fix it
                    }
                        .compositingGroup()
                        .luminanceToAlpha()
                )
                .if(model.progress != nil) { view in
                    view
                        .overlay(RoundedRectangle(cornerRadius: 5)
                            .fill(Color.white)
                            .frame(width: 3, height: 70)
                            .offset(x: innerWidth * (model.progress! - 0.5).cg)
                            .animation(nil)
                        )
                }
                
                .offset(x: geo.size.width * model.minValue.cg)
            }
            .gesture(
                DragGesture()
                    .onChanged { gesture in
                        let dragLocation = gesture.location.x
                        let frameWidth = geo.size.width
                        
                        dragHelper.newTranslation(gesture.translation)
                        let dragArea = dragHelper.horizontalDrag(dragLocation: dragLocation, frameWidth: frameWidth, leftValue: model.minValue.cg, rightValue: model.maxValue.cg)
                        let delta = dragHelper.dx  / geo.size.width
                        
                        switch(dragArea) {
                        case .leading:
                            model.updateMinValue(dragWidth: dragLocation.float, frameWidth: geo.size.width.float)
                        case .trailing:
                            model.updateMaxValue(dragWidth: dragLocation.float, frameWidth: geo.size.width.float)
                        case .center:
                            let newMin = model.minValue - delta.float
                            let newMax = model.maxValue - delta.float
                            
                            if (newMin >= 0 && newMax <= 1) {
                                model.maxValue = newMax
                                model.minValue = newMin
                                model.onMove(newLeft: newMin, newRight: newMax)
                                
                            }
                        default: break
                            
                        }
                        
                        
                    }
                    .onEnded { _ in
                        dragHelper.dragEnded()
                    }
            )
        }
        
        .padding(.horizontal)
        .animation(nil)
        
    }
}
