//
//  InspViewFrameModifier.swift
//  iosApp
//
//  Created by rst10h on 15.04.22.
//

import Foundation
import SwiftUI
import shared

//struct InspViewFrameModifier: ViewModifier {
//    
//    var inspView: InspView<AnyObject>
//    var view: ViewPlatformApple
//    var clipRect: Rect?
//
//    internal init(inspView: InspView<AnyObject>, view: ViewPlatformApple) {
//        self.inspView = inspView
//        self._view = StateObject(wrappedValue: view)
//        self.clipRect = inspView.mClipBounds
//    }
//    
//    
//    func body(content: Content) -> some View {
//        return content
//            .frame(width: inspView.viewWidth.cg, height: inspView.viewHeight.cg ) //view size todo factor
//            .clipShape(Rectangle()
//                .size(view.getCGRect().size)
//                .offset(x: view.paddingLeft.cg, y: view.paddingTop.cg)
//            )
//            .padding(.leading, view.paddingLeft.cg)
//            .padding(.trailing, view.paddingRight.cg)
//            .padding(.bottom, view.paddingBottom.cg)
//            .padding(.top, view.paddingTop.cg)
//            .border(view.borderColor, width: view.borderWidth)
//            .if(clipRect != nil) { clippedView in
//                clippedView
//                    .clipShape(Rectangle()
//                                .size(width: clipRect!.width().cg, height: clipRect!.height().cg)
//                                .offset(x: clipRect!.left.cg, y: clipRect!.top.cg)
//                    )
//            }
//            .opacity(view.viewAlpha)
//            .clipped()
//            .scaleEffect(x: view.scaleX.cg, y: view.scaleY.cg, anchor: .center) //view scale animation. todo change anchor
//            .offset(x: view.xOffset, y: view.yOffset) //view position
//            
//            .rotationEffect(.degrees(inspView.getRealRotation().double)) //view rotation
//    }
//}
