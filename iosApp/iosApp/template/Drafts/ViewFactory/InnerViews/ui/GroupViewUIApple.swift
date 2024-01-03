//
//  InspViewParentApple.swift
//  iosApp
//
//  Created by rst10h on 15.04.22.
//

import SwiftUI
import shared

//struct GroupViewUIApple: View { //unused, remove it
//    
//    @EnvironmentObject
//    var templateParent: InspTemplateViewApple
//    
////    @StateObject
////    var groupView: InspGroupViewApple
//    
//    var inspParent: InspParent
//    
//    var body: some View {
//        ZStack {
//            ForEach(templateParent.getChildren(inspParent: inspParent), id: \.self) { inspView in
//                    let type: SelectionType = inspView.getType() ?? SelectionType.nothing
//                    let viewPlatform = inspView.view as! ViewPlatformApple
//                    let clipRect: shared.Rect? = inspView.mClipBounds
//                    Group {
//                        //Text("\(templateParent.currentFrame)")
//                        switch type {
//                        case .group:
//                            GroupViewUIApple(inspParent: inspView.asInspGroupView()! as InspParent)
//                        case .vector:
//                            VectorLottieViewUI(inspView: inspView.asInspVectorView()!)
//
//                        case .text:
//                            Text(inspView.asInspTextView()!.media.text)
//
//                        case .path:
//                            Text("path")
//
//                        case .imageOrVideo:
//                            MediaViewUI(inspView: inspView.asInspMediaView()!)
//                                .scaleEffect(x: viewPlatform.innerScaleX, y: viewPlatform.innerScaleY) //inner image scale, todo: add pivot support pivot -> anchor
//                                .offset(x: viewPlatform.innerTranslationX, y: viewPlatform.innerTranslationY) // todo calculate inner offset
//
//                        default: Text("?")
//                        }
//                    }
//                    .if(inspView.viewWidth > 0 && inspView.viewHeight > 0) { view in
//                        view
//                            .frame(width: inspView.viewWidth.cg, height: inspView.viewHeight.cg ) //view size todo factor
//                            .clipShape(Rectangle() //todo this only for images
//                                .size(viewPlatform.getCGRect().size)
//                                .offset(x: viewPlatform.paddingLeft.cg, y: viewPlatform.paddingTop.cg)
//                            )
//                            .border(viewPlatform.borderColor, width: viewPlatform.borderWidth)
//                            .if(clipRect != nil) { clippedView in
//                                clippedView
//                                    .clipShape(Rectangle()
//                                                .size(width: clipRect!.width().cg, height: clipRect!.height().cg)
//                                                .offset(x: clipRect!.left.cg, y: clipRect!.top.cg)
//                                    )
//                            }
//                            .opacity(viewPlatform.viewAlpha)
//                            .clipped()
//                            .scaleEffect(x: viewPlatform.scaleX.cg, y: viewPlatform.scaleY.cg, anchor: .center) //view scale animation. todo change anchor
//                            .offset(x: viewPlatform.xOffset, y: viewPlatform.yOffset) //view position
//
//                            .rotationEffect(.degrees(inspView.getRealRotation().double)) //view rotation
//
//
//
//
//                    }
//                }
//            //Text("\(String(repeating: "\n", count: currentLevel))current:\(currentLevel)")
//        }
//    }
//}
