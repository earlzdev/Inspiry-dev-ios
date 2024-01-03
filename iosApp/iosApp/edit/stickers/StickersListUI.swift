//
//  StickersListUI.swift
//  iosApp
//
//  Created by rst10h on 2.02.22.
//

import SwiftUI
import shared

struct StickersListUI: View {
    @StateObject
    var model = StickersViewModelApple.SharedInstance()
    let onNavigationBack: (MediaWithPath?) -> ()
    
    let colors = StickersDarkColors()
    let dimens = EditDimensPhone()
    
    var body: some View {
        ZStack {
            Color(.black)
                .ignoresSafeArea()
            VStack {
                StickersTopBar(colors: colors, dimens: dimens, onBack: {
                    onNavigationBack(nil)
                    model.clearCache()
                }, onDone: {
                    onNavigationBack(model.currentStickers[model.currentStickerIndex ?? -1])
                    model.clearCache()
                })
                StickersCategories(model: model)
                StickersGrid(model: model)
                    .clipped()
            }
        }
        .statusBarStyle(.lightContent)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

struct StickersCategories: View {
    
    @ObservedObject
    var model: StickersViewModelApple
    
    let colors = StickersDarkColors()
    
    var body: some View {
        let stickers = model.categories ?? []
        let selected = model.currentCategory
        ScrollView(.horizontal, showsIndicators: false) {
            HStack {
                ForEach(stickers, id: \.self) { item in
                    Text(model.localizeTab(category: item))
                        .font(.system(size: 14.cg))
                        .foregroundColor(selected == item ? colors.tabTextActive.toSColor() : colors.tabTextInactive.toSColor())
                        .padding(.horizontal, 14.cg)
                        .padding(.vertical, 5.cg)
                        .background(selected == item ? colors.tabBgActive.toSColor() : colors.tabBgInactive.toSColor())
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                        .onTapGesture {
                            model.loadStickersForCategory(category: item)
                        }
                }
            }
        }
    }
}

struct StickersGrid: View {
    
    @ObservedObject
    var model: StickersViewModelApple
    
    let colors = StickersDarkColors()
    
    var body: some View {
        let stickers = model.currentStickers
            ScrollView(.vertical, showsIndicators: false) {
                LazyVGrid(columns: [
                    GridItem(spacing: 7, alignment: .top),
                    GridItem(spacing: 7, alignment: .top),
                    GridItem(spacing: 7, alignment: .top)
                ]) {
                    ForEach(stickers, id: \.self.path) { sticker in
                        let index = stickers.firstIndex(of: sticker)
                        let selected = index == model.currentStickerIndex
                        let strokeColor = selected ? colors.stickerStrokeActive.toSColor() : colors.stickerStrokeInactive.toSColor()
                        let tv = model.templatesWithStickers[sticker.path]!
                        ZStack {
                            TemplateUIView(templateModel: tv, autoplay: true, playSound: false)
                        }
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                        .aspectRatio(1, contentMode: .fit)
                        .background(selected ? colors.stickerBgActive.toSColor() : colors.stickerBgInactive.toSColor())
                        .clipShape(RoundedRectangle(cornerRadius: 10))
                        .overlay(RoundedRectangle(cornerRadius: 10).stroke(strokeColor, style: StrokeStyle(lineWidth: 2)))
                        .onTapGesture {
                            model.coreModel?.setCurrentStickerIndex(index: index?.int32 ?? -1)
                            
                        }
                        
                    }
                }
                
            }
            .padding(7)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
//        } else {
//            ZStack {
//                ProgressView(label: {Text("Loading...").font(.system(size: 12)).foregroundColor(Color.white)})
//                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
//                    .scaleEffect(1.5)
//            }.frame(maxWidth: .infinity, maxHeight: .infinity)
//        }
    }
}

struct StickersListUI_Previews: PreviewProvider {
    static var previews: some View {
        StickersListUI() {_ in}
    }
}
