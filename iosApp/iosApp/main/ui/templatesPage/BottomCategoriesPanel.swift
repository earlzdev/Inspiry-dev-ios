//
//  BottomCategoriesPanel.swift
//  iosApp
//
//  Created by rst10h on 24.12.21.
//

import SwiftUI
import shared

struct BottomCategoriesPanel: View {
    
    @Binding var userScroll: Bool
    @EnvironmentObject private var viewModel: MainScreenViewModelApple
    
    var body: some View {
        
        let dimens = viewModel.coreModel.getBottomCategoriesDimens()
        let colors = viewModel.coreModel.getBottomCategoriesColors()
        let categories = viewModel.coreModel.getCategories()
        ZStack {
            ScrollViewReader { sv in
                ScrollView(.horizontal, showsIndicators: false) {
                    LazyHStack {
                        ForEach(categories.indices, id: \.self) { index in
                            Itemcategory(menuItem: categories[index], selected: index == viewModel.currentCategoryIndex)
                                .onTapGesture {
                                    userScroll = false
                                    viewModel.coreModel.changeCategory(newIndex: Int32(index))
                                }
                        }
                    }
                    .padding(.horizontal, dimens.contentPadding.cg)
                    
                }
                .onChange(of: viewModel.currentCategoryIndex) { index in
                    withAnimation(.easeIn(duration: 300.0)) {
                        sv.scrollTo(index, anchor: .center)
                    }
                }
            }
            
        }
        .frame(height: dimens.listHeight.cg + 10.cg)
        .padding(.bottom, getBottomScreenInset())
        .background(colors.background.toSColor())
    }
}

struct Itemcategory: View {
    let menuItem: TemplateCategory
    let selected: Bool
    @EnvironmentObject var viewModel: MainScreenViewModelApple
    var body: some View {
        let dimens = viewModel.coreModel.getBottomCategoriesDimens()
        let colors = viewModel.coreModel.getBottomCategoriesColors()
        let textColor = selected ? colors.activeText : colors.inactiveText
        let bgColor = selected ? colors.activeBackground : colors.inactiveBackground
        ZStack(alignment: Alignment.topTrailing) {
            Text(menuItem.displayName.localized())
                .foregroundColor(textColor.toSColor())
                .font(.system(size: dimens.fontSize.cg))
                .fontWeight(.semibold)
                .padding(.horizontal, dimens.textPadding.cg)
                .frame(height: dimens.itemHeight.cg, alignment: Alignment.center)
                .background(bgColor.toSColor())
                .cornerRadius(dimens.itemRounding.cg)
            if (menuItem.icon == TemplateCategoryIcon.fire)
            {
                CyborgImage(name: "ic_fire")
                    .scaledToFill()
                    .frame(width:  dimens.iconSize.cg, height: dimens.iconSize.cg)
                    .offset(x: dimens.iconOffset.cg, y: -dimens.iconOffset.cg)
            }
        }
    }
}

struct BottomCategoriesPanel_Previews: PreviewProvider {
    @State static var userScroll = false
    static var previews: some View {
        Group {
            BottomCategoriesPanel(userScroll: $userScroll)
                .environmentObject(MainScreenViewModelApple.Create())
        }
    }
}
