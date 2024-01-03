//
//  TemplateView.swift
//  iosApp
//
//  Created by rst10h on 5.01.22.
//

import SwiftUI
import UIKit
import shared

struct InnerTemplateView: View {
    
    @EnvironmentObject
    var templateView: InspTemplateViewApple
    
    var body: some View {
        let gradient: LinearGradient? = templateView.backgroundGradient//getGradient(palette: templateView.getTemplatePalette())
        let bg = templateView.backgroundColor//gradient != nil ? Color.clear : templateView.getTemplatePalette().getBackgroundColor().ARGB
        ZStack {
            if (templateView.templateSizeAvailable && templateView.templateVisible) {
                
                InspViewParentApple(inspParent: templateView)
                    //.frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
                    .environmentObject(templateView)
            }
        }
        .aspectRatio(
            CGFloat( templateView.templateFormat().aspectRatio() ), contentMode: ContentMode.fit)
        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
        .background(bg)
        .background(gradient)
        .compositingGroup()
        .animation(nil)
        //        .onTapGesture {
        //            if (templateView.templateMode == .edit) {
        //                templateView.changeSelectedView(value: nil)
        //            }
        //        }
        
    }
   
    func getGradient(palette: TemplatePalette?) -> LinearGradient? {
        if (palette == nil) { return nil }
        var gr: LinearGradient? = nil
        let main = palette!.mainColor
        if let gradient = main as? PaletteLinearGradient {
            
            gr = gradient.getLinearGradient()
        }
        return gr
    }
}

extension PaletteLinearGradient {
    func getLinearGradient() -> LinearGradient {
        let coords = self.getShaderCoords(left: 0, top: 0, right: 1, bottom: 1)
        
        return LinearGradient(
            colors: self.colors.map {
                Color.fromInt($0.intValue)
            },
            startPoint: UnitPoint(
                x: CGFloat(coords.get(index: 0)),
                y: CGFloat(coords.get(index: 2))
            ),
            endPoint: UnitPoint(
                x: CGFloat(coords.get(index: 1)),
                y: CGFloat(coords.get(index: 3))
            )
        )
    }
    func getCAGradientLayer() -> CAGradientLayer {
        let coords = self.getShaderCoords(left: 0, top: 0, right: 1, bottom: 1)
        let gradient = CAGradientLayer()
        gradient.colors = self.colors.map {
            Color.fromInt($0.intValue).cgColor
        }
        gradient.startPoint = CGPoint(
            x: CGFloat(coords.get(index: 0)),
            y: CGFloat(coords.get(index: 2))
        )
        gradient.endPoint = CGPoint(
            x: CGFloat(coords.get(index: 1)),
            y: CGFloat(coords.get(index: 3))
        )
        
        return gradient
    }
}

struct TemplateView_Previews: PreviewProvider {
    static var previews: some View {
        InnerTemplateView()
            .environmentObject(InspTemplateViewApple.fakeInitializedTemplate())
    }
}
