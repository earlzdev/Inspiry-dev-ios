//
//  FontCategoriesView.swift
//  iosApp
//
//  Created by vlad on 13/7/21.
//

import SwiftUI
import shared

struct FontCategoriesView: View {
    var colors: FontDialogColors
    var dimens: FontDialogDimens
    
    var categories: [String]
    var selectedCategoryIndex: Int
    var onSelectedChange: (Int) -> ()
    
    var body: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing:0) { //lazyHStack is preferable, but it has a bug with animation
                Spacer()
                    .frame(width: CGFloat(dimens.categoryContentPadding))
                
                ForEach(Array(zip(categories.indices, categories)), id: \.1) { index, item in
                    
                    let isSelected = index == selectedCategoryIndex
                    
                    Button(action : {  onSelectedChange(index)}, label: {
                        Text(item.capitalized).foregroundColor(isSelected ? colors.categoryTextActive.toSColor() : colors.categoryTextInactive.toSColor())
                            .font(.system(size: CGFloat(dimens.categoryTextSize)))
                            .fontWeight(.light)
                            
                            .frame(alignment: .center)
                            .frame(maxHeight: .infinity)
                            .padding(.horizontal, CGFloat(dimens.categoryItemPaddingHorizontal))

                            .background(isSelected ? colors.categoryBgActive.toSColor() : Color.clear)
                           
                            .clipShape(RoundedRectangle(cornerRadius: CGFloat(dimens.categoryBgClip)))
                    })
                }
                
                Spacer()
                    .frame(width: CGFloat(dimens.categoryContentPadding))
            }
        }.frame(height: CGFloat(dimens.categoryHeight), alignment: .center)
        .frame(maxWidth: .infinity)
    }
}

struct FontCategoriesView_Previews: PreviewProvider {
    static var previews: some View {
        FontCategoriesView(colors: FontDialogColorsDark(),
                           dimens: FontDialogDimensPhone(), categories: ["Upload", "Simple", "Script", "Brush", "Nice"], selectedCategoryIndex: 0, onSelectedChange: { _ in
                            
                           }).background(FontDialogColorsDark().backgroundColor.toSColor())
    }
}
