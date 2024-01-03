//
//  DebugPanelUI.swift
//  iosApp
//
//  Created by rst10h on 11.03.22.
//

import SwiftUI
import shared

struct DebugPanelUI: View {
    @EnvironmentObject
    var model: EditViewModelApple
    
    var body: some View {
        VStack {
            Text("test")
                .foregroundColor(.white)
                .frame(height: 60.cg ,alignment: .center)
            Text("select any text view")
                .foregroundColor(.white)
                .frame(height: 60.cg ,alignment: .center)
                .onTapGesture {
                    model.instrumentsManager.mayRemoveAdditionalPanel()
                    let tv = model.templateView.allTextViews.filter{$0.isDuplicate() == false}.first
                    model.templateView.changeSelectedView(value: tv?.asGeneric())
                }
            Text("toggle debug frames. (debug frames is \(model.templateView.debugFrames ? "on" : "off"))")
                .foregroundColor(.white)
                .frame(height: 60.cg ,alignment: .center)
                .onTapGesture {
                    model.instrumentsManager.mayRemoveAdditionalPanel()
                    model.templateView.debugFrames.toggle()
                }
            Text("update template notify")
                .foregroundColor(.white)
                .frame(height: 60.cg ,alignment: .center)
                .onTapGesture {
                    model.instrumentsManager.mayRemoveAdditionalPanel()
                    model.notifyTemplateChanged()
                }
            Text("display demo sources")
                .foregroundColor(.white)
                .frame(height: 60.cg ,alignment: .center)
                .onTapGesture {
                    model.instrumentsManager.mayRemoveAdditionalPanel()
                    model.coreModel?.setDemoToAllImages()
                    model.notifyTemplateChanged()
                }
        }
    }
}

struct DebugPanelUI_Previews: PreviewProvider {
    static var previews: some View {
        DebugPanelUI()
    }
}
