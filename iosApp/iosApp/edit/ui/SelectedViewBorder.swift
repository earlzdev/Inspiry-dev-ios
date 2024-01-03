//
//  SelectedViewBorder.swift
//  iosApp
//
//  Created by rst10h on 30.06.22.
//

import SwiftUI
import shared

struct SelectedViewBorder: View {
    @EnvironmentObject
    var wrapperHelper: EditWrapperHelperApple
    
    var rotate: some Gesture {
        DragGesture(coordinateSpace: .global)
            .onChanged { point in
                wrapperHelper.rotationAction(touchPoint: CGPoint(x: point.translation.width, y: point.translation.height))
            }
            .onEnded { _ in
                wrapperHelper.endRotation()
            }
    }
    var scale: some Gesture {
        DragGesture(coordinateSpace: .global)
            .onChanged { point in
                wrapperHelper.scaleAction(translate: CGSize(width: point.translation.width, height: point.translation.height))
            }
            .onEnded { _ in
                wrapperHelper.endScale()
            }
    }
    
    var body: some View {
        let size = wrapperHelper.editBounds.size
        if (wrapperHelper.isVisible && size != .zero) {
            let x = wrapperHelper.editBounds.minX
            let y = wrapperHelper.editBounds.minY
            let actions = wrapperHelper.availableActions
            ZStack {
                
                Rectangle()
                    .stroke(lineWidth: 2)
                    .foregroundColor(Color.gray)
                if (actions.contains(.buttonDuplicate)) {
                    CyborgImage(name: "ic_copy_text")
                        .scaledToFill()
                        .frame(width: 25, height: 25)
                        .shadow(radius: 5)
                        .padding(5)
                        .background(Color.white.opacity(0.01))
                        .offset(x: -size.width/2, y: -size.height/2)
                        .zIndex(1000)
                        .onTapGesture{
                            wrapperHelper.coreHelper.doCopyAction()
                        }
                }
                
                if (actions.contains(.buttonClose)) {
                    CyborgImage(name: "ic_delete_text")
                        .scaledToFill()
                        .frame(width: 25, height: 25)
                        .shadow(radius: 5)
                        .padding(5)
                        .background(Color.white.opacity(0.01))
                        .offset(x: size.width/2, y:  -size.height/2)
                        .onTapGesture {
                            wrapperHelper.coreHelper.templateView.selectedView?.asInspMediaView()?.pauseVideoIfExists()
                            wrapperHelper.coreHelper.removeAction()
                            wrapperHelper.coreHelper.templateView.objectWillChanged()
                        }
                }
                
                if (actions.contains(.buttonRotate)) {
                    CyborgImage(name: "ic_rotation_text")
                        .scaledToFill()
                        .frame(width: 25, height: 25)
                        .shadow(radius: 5)
                        .padding(5)
                        .background(Color.white.opacity(0.01))
                        .offset(x: -size.width/2, y: size.height/2)
                        .gesture(rotate)
                    
                }
                
                if (actions.contains(.buttonScale)) {
                    CyborgImage(name: "ic_scale_round_text")
                        .scaledToFill()
                        .frame(width: 25, height: 25)
                        .shadow(radius: 5)
                        .padding(5)
                        .background(Color.white.opacity(0.01))
                        .offset(x: size.width/2, y: size.height/2)
                        .gesture(scale)
                }
            }
            .frame(width: size.width+5,height: size.height+5)
            .padding(30)
            .rotationEffect(Angle(degrees: wrapperHelper.rotation.double))
            .offset(x: x, y: y)
            .animation(.none)
        } else {
            EmptyView()
        }
    }
}

//
//struct SelectedViewBorder_Previews: PreviewProvider {
//    static var previews: some View {
//        SelectedViewBorder()
//    }
//}
