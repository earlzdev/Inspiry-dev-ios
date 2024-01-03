//
//  ScalableTrimVideoSlider.swift
//  iosApp
//
//  Created by rst10h on 17.10.22.
//

import SwiftUI
import shared

public struct ScalableTrimSlider: View {
    
    static let icon_width: CGFloat = 8
    static let icon_padding: CGFloat = 4
    static let thumbnailsPadding = icon_width + icon_padding * 2
    static let horizontalSpacing = 50.cg
    
    @ObservedObject
    var model: ScalableTrimSliderModel
    
    let containerWidth = UIScreen.screenWidth - horizontalSpacing * 2
    
    private let dragHelper = ScalableTrimDragHelper()
    
    public var body: some View {
        OffsetableScrollView([]) {
            GeometryReader { geo in
                let relativeWidth = model.localRight.cg - model.localLeft.cg
                ZStack (alignment: Alignment(horizontal: .leading, vertical: .center)) {
                    
                    //preview thumbnails
                    InspTrimVideoView(model: model)
                        .frame(height: 50)
                        .gesture(DragGesture().onChanged( { _ in model.isUserAction = true }))
                    
                    //leading and trailing shading
                    HStack(spacing: 0) {
                        if (model.leftShadingSize - model.leadShadingOffset > 0) {
                            Rectangle()
                                .fill(0x9ccccccc.ARGB)
                                .frame(width: model.leftShadingSize, height: 50)
                        }
                        Spacer()
                        if (model.rightShadingSize - model.trailShadingOffset > 0) {
                            Rectangle()
                                .fill(0x9ccccccc.ARGB)
                                .frame(width: model.rightShadingSize, height: 50)
                        }
                    }
                    .animation(model.currentGesutre == nil && !model.isUserAction ? .easeInOut : nil)
                    .mask(Rectangle() //left and right shadow cut by thumbnails size
                        .padding(.leading, model.leadShadingOffset)
                        .padding(.trailing, model.trailShadingOffset)
                        .animation(nil)
                    )
                    
                    //leading and trailing knobs
                    HStack(spacing: 0) {
                        CyborgImage(name: "ic_arrow_trim_left")
                            .frame(width: 8, height: 15)
                            .padding(.leading, Self.icon_padding)
                            .frame(width: Self.thumbnailsPadding, height: 60)
                            .background(Color.white)
                        Spacer(minLength: 0)
                        CyborgImage(name: "ic_arrow_trim_right")
                            .frame(width: 8, height: 15)
                            .padding(.trailing, Self.icon_padding)
                            .frame(width: Self.thumbnailsPadding, height: 60)
                            .background(Color.white)
                    }
                    .frame(height: 60, alignment: .center)
                    .border(Color.white, width: 5)
                    .cornerRadius(10)
                    .padding(.trailing, Self.horizontalSpacing - Self.thumbnailsPadding + model.trailingKnobPosition)
                    .padding(.leading, Self.horizontalSpacing - Self.thumbnailsPadding - model.leadingKnobPosition)
                    .padding(.vertical, 5)
                    .animation(model.currentGesutre == nil ? .easeInOut : nil)
                    .simultaneousGesture(
                        DragGesture(coordinateSpace: .local)
                            .onChanged { gesture in
                                model.isUserAction = true
                                print("gesture process")
                                let dragLocation = gesture.location.x
                                let frameWidth = geo.size.width
                                
                                dragHelper.newTranslation(gesture.translation)
                                let dragArea = model.currentGesutre ?? dragHelper.horizontalDrag(dragLocation: dragLocation, frameWidth: frameWidth, leftValue: model.localLeft.cg, rightValue: model.localRight.cg)
                                switch(dragArea) {
                                case .leading:
                                    print("leading drag")
                                    model.dragLeading(dx: dragHelper.dx, frameWidth: frameWidth - Self.horizontalSpacing * 2)
                                case .trailing:
                                    model.dragTrailing(dx: dragHelper.dx, frameWidth: frameWidth - Self.horizontalSpacing * 2)
                                case .center:
                                    break
                                default: break
                                }
                                if model.currentGesutre == nil { model.currentGesutre = dragArea }
                                
                            }
                            .onEnded { gesture in
                                print("drag ended")
                                dragHelper.dragEnded()
                                model.currentGesutre = nil
                                model.isUserAction = false
                                //withAnimation {
                                    model.resetKnobs(containerWidth: containerWidth)
                                //}
                            }
                    )
                    .overlay(ZStack(alignment: .center) {
                        let string = model.getCurrentTimeString()
                        let leftOffset = model.leadingKnobPosition < 0 ? -model.leadingKnobPosition : 0
                        let rightOffset = model.trailingKnobPosition > 0 ? model.trailingKnobPosition : 0
                        let offset = -rightOffset + leftOffset
                        var progressIndicatorOffset: CGFloat = model.currentGesutre == .trailing
                        ? -model.trailingKnobPosition : model.currentGesutre == .leading ? -model.leadingKnobPosition: 0
                        
                        if (model.progress != nil) {
                            RoundedRectangle(cornerRadius: 5)
                                .fill(Color.white)
                                .frame(width: 3, height: 70)
                                .offset(x: containerWidth * (model.progress! - 0.5).cg + progressIndicatorOffset)
                        }
                        Text(string)
                            .font(.system(size: 15))
                            .foregroundColor(.white)
                            .fontWeight(.bold)
                            .padding(5)
                            .background(0x8d303030.ARGB)
                            .cornerRadius(5)
                            .offset(x: offset / 2)
                    }
                        .allowsHitTesting(false)
                        .animation(model.currentGesutre != nil || model.isPlaying ? nil : .easeInOut)
                    )
                }
                .animation(.easeInOut)
                
            }
            .animation(nil)
        }
    }
}

//struct ScalableTrimSlider_Previews: PreviewProvider {
//    static var min: Float = 0
//    
//    static var max: Float = 1
//    
//    static var progress: Float? = 0.3
//    
//    static let url: URL? = nil //Bundle.main.url(forResource: "countdown", withExtension: "mp4")! //test video for preview
//    
//    static var previews: some View {
//        VStack(alignment: .center) {
//            ScalableTrimSlider(model:  ScalableTrimSliderModel(url: url, minValue: min, maxValue: max, progress: progress))
//        }
//        .frame(alignment: .center)
//        .background(0xff202020.ARGB)
//    }
//}

