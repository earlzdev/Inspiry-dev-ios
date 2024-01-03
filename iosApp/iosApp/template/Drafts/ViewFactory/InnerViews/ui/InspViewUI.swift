//
//  InspViewUI.swift
//  iosApp
//
//  Created by rst10h on 18.05.22.
//

import SwiftUI
import shared

struct InspViewUI: View {
    
    @EnvironmentObject
    var templateParent: InspTemplateViewApple
    
    weak var inspView: InspView<AnyObject>?
    
    var body: some View {
        if let inspView = inspView,
           let animationHelper = inspView.animationHelper,
           let viewPlatform = (inspView.view as? ViewPlatformApple) {
            let type: SelectionType = inspView.getType() ?? SelectionType.nothing
            let clipRect: shared.Rect? = inspView.mClipBounds
            let cornerRadius: CGFloat = inspView.getCornerRadiusAbsolute().cg
            let stickCorners: Bool = inspView.stickCorners()
            let invertedMaskPath: Bool = (animationHelper as? AnimationHelperApple)?.maskPath.inverted ?? false
            let scaledHeight: CGFloat = viewPlatform.scaledHeight
            let scaledWidth: CGFloat = viewPlatform.scaledWidth
            let isNotSingularScale = (viewPlatform.scaleY * viewPlatform.scaleX) > 0.0001
            let isNotSingular = isNotSingularScale && scaledWidth >= 0.5 && scaledHeight >= 0.5 && (clipRect?.width() ?? 1) >= 1 && (clipRect?.height() ?? 1) >= 1 && viewPlatform.viewAlpha > 0 //&& (animationHelper as? AnimationHelperApple)?.notSingularMask() != false
            let maxSize = max(scaledWidth, scaledHeight)
            let circularSize = maxSize * (animationHelper.circularOutlineClipRadiusDegree?.floatValue.cg ?? 0)
            if (isNotSingular) {
                Group {
                    switch type {
                    case .group:
                        InspViewParentApple(inspParent: inspView.asInspGroupView()! as InspParent)
                        // .allowsHitTesting(false)
                        
                        
                    case .vector:
                        VectorLottieViewUI(inspView: inspView.asInspVectorView()!)
                            .allowsHitTesting(inspView.touchesEnabled())
                        
                    case .text:
                        if let t = inspView.asInspTextView() {
                            TextViewUI(inspView: t)
                                .allowsHitTesting(inspView.touchesEnabled())
                        }
                        
                    case .path:
                        if let p = inspView.asInspPathView()?.drawPath() as? ApplePath {
                            if (p.paintStyle == .stroke && !p.path.isEmpty) {
                                p.path
                                    .stroke(lineWidth: p.strokeWidth.cg)
                                    .fill(p.color)
                                    .drawingGroup()
                                    .clipped()
                                    .allowsHitTesting(inspView.touchesEnabled())
                            }
                            if (p.paintStyle == .fill && !p.path.isEmpty) {
                                p.path
                                    .fill(p.color)
                                    .drawingGroup()
                                    .allowsHitTesting(inspView.touchesEnabled())
                                
                            }
                        }
                        
                        
                    case .imageOrVideo:
                        if let im = inspView.asInspMediaView() {
                            
                            MediaViewUI(inspView: im)
                                .blur(radius: (inspView.asInspMediaView()?.innerMediaView as? InnerMediaViewApple)?.blurRadius ?? 0)
                                .scaleEffect(x: viewPlatform.innerScaleX, y: viewPlatform.innerScaleY, anchor: UnitPoint(x: im.media.innerPivotX.cg, y: im.media.innerPivotY.cg))
                                .offset(x: viewPlatform.innerTranslationX, y: viewPlatform.innerTranslationY) // todo calculate inner offset
                                .allowsHitTesting(inspView.touchesEnabled())
                        }
                        
                    case .videoDemo:
                        VideoDemoUI(inspView: inspView.asInspSimpleVideoView()!)
                            .frame(width: inspView.viewWidth.cg, height: inspView.viewHeight.cg)
                        
                    default: Text("????????")
                    }
                }
                .if(type != .videoDemo) { view in
                    view
                        .frame(width: scaledWidth, height: scaledHeight ) //view size todo factor
                        .modifier(ViewPlatformModifier(color: viewPlatform.backgroundColor, gradient: viewPlatform.backgroundGradient))
                        .padding(EdgeInsets(top: viewPlatform.marginTop, leading: viewPlatform.marginLeft, bottom: viewPlatform.marginBottom, trailing: viewPlatform.marginRight))
                        .if(inspView.asInspMediaView() != nil) { v in
                            v
                                .clipShape(Rectangle() //todo this only for image?
                                    .size(width: viewPlatform.widthWPadH, height: viewPlatform.heightWPadV)
                                    .offset(x: viewPlatform.paddLeft, y: viewPlatform.paddTop)
                                )
                        }
                    
                        .if(!stickCorners && cornerRadius != 0.cg) { vv in
                            vv.cornerRadius(cornerRadius, corners: inspView.media.cornerRadiusPosition?.corners ?? .allCorners)
                        }
                    
                        .if(viewPlatform.borderWidth != 0.cg) { vv in
                            vv.overlay(RoundedRectangle(cornerRadius: cornerRadius).stroke(viewPlatform.borderColor, lineWidth: viewPlatform.borderWidth))
                        }
                    
                        .if(clipRect != nil && animationHelper.circularOutlineClipRadiusDegree == nil) { clippedView in
                            clippedView
                                .clipShape(RoundedCorner(radius: stickCorners ? cornerRadius : 0, corners: inspView.media.cornerRadiusPosition?.corners ?? .allCorners)
                                    .size(width: clipRect!.width().cg, height: clipRect!.height().cg)
                                    .offset(x: clipRect!.left.cg, y: clipRect!.top.cg)
                                )
                        }
                    
                        .if(circularSize > 0) { circledView in
                            circledView
                                .clipShape(Circle()
                                    .size(width: circularSize, height: circularSize)
                                    .offset(x: (scaledWidth - circularSize)/2.cg, y: (scaledHeight - circularSize)/2.cg)
                                )
                        }
                        .opacity(viewPlatform.viewAlpha)
                        .if(inspView.hasTextureMask()) { maskedView in
                            maskedView.mask( //mask
                                InspViewUI(inspView: templateParent.getMaskView(viewId: inspView.media.id!))
                                    .if(inspView.getTemplateMaskOrNull()?.invertFragmentAlpha == true) { mask in
                                        mask
                                            .background(Color.white)
                                            .compositingGroup()
                                            .luminanceToAlpha()
                                    }
                                    .environmentObject(templateParent)
                            )
                        }
                        .if(animationHelper.hasClipPath()) { clippedView in
                            clippedView
                                .mask(
                                    ShapeFromPath(path:(animationHelper as! AnimationHelperApple).maskPath.path)
                                        .fill(invertedMaskPath ? Color.black : Color.white)
                                        .background(invertedMaskPath ? Color.white : Color.black)
                                        .compositingGroup()
                                        .luminanceToAlpha()
                                    //.allowsHitTesting(false)
                                )
                        }
                        .scaleEffect(x: viewPlatform.scaleX.cg, y: viewPlatform.scaleY.cg, anchor: viewPlatform.getInnerPivot(inspView: inspView)) //view scale animation. todo change anchor
                        .rotationEffect(.degrees(inspView.getRealRotation().double)) //view rotation
                        .offset(x: viewPlatform.xOffset, y: viewPlatform.yOffset) //view position
                }
            }
        }
        else { Rectangle().fill(Color.red)}
    }
    
}


extension InspView {
    @objc func stickCorners() -> Bool {
        return self.animationHelper?.clipStickingCorners ?? false
    }
}
struct ViewPlatformModifier: ViewModifier {
    let color: SwiftUI.Color
    let gradient: LinearGradient?
    
    func body(content: Content) -> some View {
        if let gradient = gradient {
            content
                .background(gradient)
        } else {
            content
                .background(color)
        }
    }
}
