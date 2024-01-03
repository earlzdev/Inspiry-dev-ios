//
//  TrimVideoSlider.swift
//  iosApp
//
//  Created by rst10h on 25.07.22.
//

import SwiftUI
import shared

public struct TrimVideoSliderUI: View {
    
    static let icon_width: CGFloat = 8
    static let icon_padding: CGFloat = 2.5
    static let thumbnailsPadding = icon_width + icon_padding + 2.cg
    
    @State
    var isDragged = false
    
    @ObservedObject
    var model: TrimVideoModel
    
    let containerWidth = UIScreen.screenWidth
    
    private let dragHelper = DragGestureHelper()
    
    public var body: some View {
        OffsetableScrollView([]) {
            GeometryReader { geo in
                let selectedWidth = geo.size.width * (model.maxValue.cg - model.minValue.cg)
                let innerWidth = selectedWidth - 30
                ZStack (alignment: Alignment(horizontal: .leading, vertical: .center)) {
                    
                    
                    ThumbnailsViewRelresentable(trimModel: model)
                        .background(Color.gray)
                        .cornerRadius(11)
                        .frame(height: 50)
                        .overlay(
                            RoundedRectangle(cornerRadius: 10).strokeBorder(Color.black, lineWidth: 3)
                        )
                        .padding(.horizontal, Self.thumbnailsPadding)
                        .offset(x: model.previewOffset)
                        .animation(.easeOut(duration: 0.3))
                    ZStack {
                        
                    }
                    .frame(width: selectedWidth, height: 60, alignment: .center)
                    .background(Color.white)
                    .cornerRadius(10)
                    .overlay(HStack(spacing: 0) {
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
                    DragGesture(coordinateSpace: .local)
                        .onChanged { gesture in
                            print("gesture process")
                            let dragLocation = gesture.location.x
                            let frameWidth = geo.size.width
                            
                            dragHelper.newTranslation(gesture.translation)
                            let dragArea = dragHelper.horizontalDrag(dragLocation: dragLocation, frameWidth: frameWidth, leftValue: model.minValue.cg, rightValue: model.maxValue.cg)
                            let delta = dragHelper.dx  / geo.size.width
                            if (!isDragged) {
                                switch(dragArea) {
                                case .leading:
                                    model.updateMinValue(dragWidth: dragLocation.float, frameWidth: geo.size.width.float)
                                case .trailing:
                                    model.updateMaxValue(dragWidth: dragLocation.float, frameWidth: geo.size.width.float)
                                case .center:
                                    let newMin = model.minValue - delta.float
                                    let newMax = model.maxValue - delta.float
                                    //if not draggable thumbnails:
                                    if (model.offScreenSize == 0) {
                                        if (newMin >= 0 && newMax <= 1) {
                                            model.setNewRangeForMove(left: newMin, right: newMax)
                                        }
                                    }
                                    else {
                                        withAnimation {
                                            model.setNewOffset(rawValueDx: dragHelper.dx)
                                        }
                                        isDragged = true
                                    }
                                    
                                default: break
                                    
                                }
                            } else {
                                model.setNewOffset(rawValueDx: dragHelper.dx)
                            }
                            
                            
                        }
                        .onEnded { gesture in
                            if (isDragged) {
                                let dragLocation = gesture.predictedEndLocation.x
                                let frameWidth = geo.size.width
                                
                                dragHelper.newTranslation(gesture.predictedEndTranslation)
                                let dragArea = dragHelper.horizontalDrag(dragLocation: dragLocation, frameWidth: frameWidth, leftValue: model.minValue.cg, rightValue: model.maxValue.cg)
                                let delta = dragHelper.dx  / geo.size.width
                                model.setNewOffset(rawValueDx: dragHelper.dx)
                            }
                            dragHelper.dragEnded()
                            isDragged = false
                        }
                )
            }
            .frame(width: model.videoCGWidth)
            .animation(nil)
        }
        
        
    }
}

struct TrimVideoSlider_Previews: PreviewProvider {
    static var min: Float = 0
    
    static var max: Float = 1
    
    static var progress: Float? = 0.5
    
    static let url: URL? = nil //Bundle.main.url(forResource: "countdown", withExtension: "mp4")! //test video for preview
    
    static var previews: some View {
        VStack(alignment: .center) {
            TrimVideoSliderUI(model:  TrimVideoModel(url: url, minValue: min, maxValue: max, progress: progress))
        }
        .frame(alignment: .center)
        .background(0xff202020.ARGB)
    }
}
