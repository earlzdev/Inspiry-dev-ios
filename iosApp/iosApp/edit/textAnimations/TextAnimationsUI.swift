//
//  TextAnimationsUI.swift
//  iosApp
//
//  Created by rst10h on 2.02.22.
//

import SwiftUI
import shared

struct TextAnimationsUI: View {
    let colors: StickersColors = StickersDarkColors()
    let dimens: EditDimens = EditDimensPhone()
    
    @StateObject
    var model: TextAnimViewModelApple
    
    let onNavigationBack: (String?) -> ()
    
//    init(text: String?, onNavigationBack: @escaping (String?) -> ()) {
//        self.colors = StickersDarkColors()
//        self.dimens = EditDimensPhone()
//        self._model = StateObject(wrappedValue: TextAnimViewModelApple(text: text))
//        self.onNavigationBack = onNavigationBack
//    }
    
    var body: some View {
        NavigationView {
            ZStack {
                Color(0xff202020.ARGB.cgColor!)
                    .ignoresSafeArea()
                NavigationLink(
                    destination: SubscribeUIView(source: Constants.TAG_ANIMATION_VIEW) {
                        print("return from subscribe..")
                        model.fullScreenSelection = nil
                    }
                        .navigationBarHidden(true), tag: Constants.TAG_SUBSCRIBE_VIEW,
                    selection: $model.fullScreenSelection) { EmptyView() }
                
                VStack(spacing: 0) {
                    StickersTopBar(colors: colors, dimens: dimens, onBack: { onNavigationBack(nil) }, onDone:
                                    {
                        if model.onSaveClick() {
                            onNavigationBack(model.coreModel.selectedAnimationPath?.originalPath)
                        }
                        
                    })
                    .background(0xff2b2b2b.ARGB)
                    ZStack {
                        if let tv = model.topPreview {
                            TemplateUIView(templateModel: tv, autoplay: true, playSound: false)
                        } else {
                            Text("preview n/a!")
                                .foregroundColor(.red)
                        }
                    }
                    .background(0xff2b2b2b.ARGB)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .aspectRatio(2, contentMode: .fit)
                    
                    TextAnimCategories(model: model)
                    TextAnimGrid(model: model)
                        .clipped()
                }
            }
            .statusBarStyle(.lightContent)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }
}

struct TextAnimCategories: View {
    
    @ObservedObject
    var model: TextAnimViewModelApple
    
    let colors = StickersDarkColors()
    
    var body: some View {
        let categories = model.tabs
        let selected = model.currentCategory
        ScrollView(.horizontal, showsIndicators: false) {
            HStack {
                ForEach(categories, id: \.self) { item in
                    let tabIndex = categories.firstIndex(of: item)
                    Text(item)
                        .font(.system(size: 14.cg))
                        .bold()
                        .foregroundColor(selected == item ? colors.tabTextActive.toSColor() : colors.tabTextInactive.toSColor())
                        .padding(.horizontal, 20.cg)
                        .padding(.vertical, 5.cg)
                        .background(selected == item ? colors.tabBgActive.toSColor() : colors.tabBgInactive.toSColor())
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                        .onTapGesture {
                            model.coreModel.showAnimationsFromTab(tab: tabIndex?.int32 ?? 0, previewFirstTemplate: true, forPreviewMediaPath: nil)
                        }
                }
            }
            .padding(.horizontal, 15)
            .padding(.vertical, 12)
        }
    }
}

struct TextAnimGrid: View {
    
    @ObservedObject
    var model: TextAnimViewModelApple
    let colors = StickersDarkColors()
    @State
    var selectedIndex = 0
    @EnvironmentObject
    private var licenseManagerWrapper: LicenseManagerAppleWrapper
    
    var body: some View {
        if let textAnims = model.currentAnimations {
            ScrollView(.vertical, showsIndicators: false) {
                LazyVGrid(columns: [
                    GridItem(spacing: 7, alignment: .top),
                    GridItem(spacing: 7, alignment: .top),
                    GridItem(spacing: 7, alignment: .top)
                ]) {
                    ForEach(textAnims, id: \.self) { anim in
                        let index = textAnims.firstIndex(of: anim)
                        let selected = index == selectedIndex
                        let strokeColor = selected ? colors.stickerStrokeActive.toSColor() : colors.stickerStrokeInactive.toSColor()
                        if let tv = model.templatesWithAnims[anim] {
                            ZStack(alignment: .topTrailing) {
                                TemplateUIView(templateModel: tv, autoplay: true, playSound: false)
                                if (anim.media.forPremium() && !licenseManagerWrapper.hasPremium) {
                                    Text("PRO")
                                        .foregroundColor(colors.tabTextInactive.toSColor())
                                        .font(.system(size: 9))
                                        .padding(.horizontal, 4)
                                        .padding(.vertical, 2)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 7)
                                                .stroke(colors.tabTextInactive.toSColor(), lineWidth: 1)
                                        )
                                        .offset(x: -3, y: 3)
                                }
                            }
                            .frame(maxWidth: .infinity, maxHeight: .infinity)
                            .aspectRatio(1.5, contentMode: .fit)
                            //.background(selected ? colors.stickerBgActive.toSColor() : colors.stickerBgInactive.toSColor())
                            .background(0xff2b2b2b.ARGB)
                            .clipShape(RoundedRectangle(cornerRadius: 10))
                            .overlay(RoundedRectangle(cornerRadius: 10).stroke(strokeColor, style: StrokeStyle(lineWidth: 2)))
                            .onTapGesture {
                                selectedIndex = index ?? 0
                                model.coreModel.onClickTemplateInList(data: anim)
                            }
                            .padding(2)
                        }
                        
                    }
                }
                
            }
            //.padding(7)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else {
            ZStack {
                ProgressView(label: {Text("Loading...").font(.system(size: 12)).foregroundColor(Color.white)})
                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                    .scaleEffect(1.5)
            }.frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }
}

struct TextAnimationsUI_Previews: PreviewProvider {
    static var previews: some View {
        TextAnimationsUI(model: TextAnimViewModelApple(text: "test")) { _ in }
    }
}
