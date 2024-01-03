//
//  ColorDialogView.swift
//  iosApp
//
//  Created by rst10h on 2.02.22.
//

import SwiftUI
import shared

struct ColorDialogUI: View {
    
    @StateObject
    var model: ColorDialogModelApple
    
    var body: some View {
        VStack {
            ColorDialogPagesUI()
                .padding(.bottom, model.dimens.pageTextVerticalPadding.cg)
            Group {
                switch model.currentPage {
                case .color:
                    ColorItemsGroup()
                case .gradient:
                    GradientItemsGroup()
                case .palette:
                    PaletteItemGroup()
                case .opacity:
                    OpacityGroup()
                case .image:
                    ImageGroup()
                default:
                    Text("not implemented yet")
                        .foregroundColor(.white)
                        .frame(height: 80)
                    
                }
            }
            .transition(.opacity.combined(with: .offset(y: model.dimens.itemListHeight.cg)))
        }
        .padding(.vertical)
        .background(model.colors.background.toSColor())
        .environmentObject(model)
        
    }
}

struct ColorDialogView_Previews: PreviewProvider {
    static let tv = InspTemplateViewApple.fakeInitializedTemplate()
    static let model = ColorDialogModelApple(tv){}
    static var previews: some View {
        ColorDialogUI(model: model)
    }
}
