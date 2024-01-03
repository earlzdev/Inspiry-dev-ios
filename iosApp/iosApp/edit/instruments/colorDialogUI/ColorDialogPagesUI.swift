//
//  ColorDialogPagesUI.swift
//  iosApp
//
//  Created by rst10h on 2.02.22.
//

import SwiftUI
import shared

struct ColorDialogPagesUI: View {
    
    @EnvironmentObject
    var model: ColorDialogModelApple
    
    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 0) {
                let pages = model.pages()
                ForEach( pages.getKeys(), id: \.self) { page in
                    Button(action: {
                        withAnimation {
                            model.onPageSelected(page)
                        }
                    }) {
                        if (model.pageIsAvailable(page)) {
                            PageChooser(
                                menuItem: pages.getMenuItem(item: page),
                                isSelected: model.currentPage == page)
                        }
                    }
                }
            }
            .padding(.leading, model.dimens.panelStartPadding.cg)
        }
    }
}

struct PageChooser: View {
    
    let menuItem: CommonMenuItem<ColorDialogPage>
    let isSelected: Bool
    
    @EnvironmentObject
    var model: ColorDialogModelApple
    
    var body: some View {
        let backgroundColor = isSelected ? model.colors.selectedPageTextBackground.toSColor() : Color.black.opacity(0)
        let textColor = isSelected ? model.colors.selectedPageText.toSColor() : model.colors.pageText.toSColor()
        HStack {
            if (menuItem.icon != nil) {
                CyborgImage(name: menuItem.icon!)
                    .scaledToFill()
                    .frame(width: 11, height: 11)
                    .colorMultiply(textColor)
            }
            Text(menuItem.text.localized().uppercased())
                .font(.system(size: model.dimens.tabTextSize.cg))
                .foregroundColor(textColor)
            
        }
        .padding(.horizontal, model.dimens.pageTextHorizontalPadding.cg)
        .padding(.vertical, model.dimens.pageTextVerticalPadding.cg)
        .background(Capsule().fill().foregroundColor(backgroundColor))
        
    }
}

struct ColorDialogPagesUI_Previews: PreviewProvider {
    static var previews: some View {
        ColorDialogPagesUI()
            .environmentObject(ColorDialogModelApple(InspTemplateViewApple.fakeInitializedTemplate()){})
            .preferredColorScheme(.dark)
    }
}
