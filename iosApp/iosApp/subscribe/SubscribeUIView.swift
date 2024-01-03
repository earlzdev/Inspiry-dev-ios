//
//  SubscribeUIView.swift
//  iosApp
//
//  Created by vlad on 12/11/21.
//

import SwiftUI
import shared

struct SubscribeUIView: View {
    let source: String
    let onNavigationBack: () -> ()
    let colors = SubscribeColorsLight()
    let dimens = SubscribeDimensPhoneH700()
    @StateObject
    var model: SubscribeViewModel
    
    init(source: String, onNavigationBack: @escaping () -> Void) {
        self.source = source
        self.onNavigationBack = onNavigationBack
        self._model = StateObject(wrappedValue: SubscribeViewModel(source: source))
    }
    
    var body: some View {
        ZStack {
            VStack {
                //header
                SubscribeHeader(colors: colors, dimens: dimens, onNavigationBack: onNavigationBack)
                    .frame(minHeight: 0, alignment: .bottom)
                    .clipped()
                    .overlay(ZStack {
                        Button(action: onNavigationBack, label: {
                            CyborgImage(name: "ic_subscribe_close")
                                .scaledToFill()
                                .padding(8)
                                .frame(width: 32, height: 32)
                            
                        }
                               
                        )
                        .padding(.horizontal, 20)
                        .padding(.vertical, UIScreen.statusBarHeight + 5.cg)
                    }
                        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity, alignment: .topLeading)
                    )
                FeatureList(colors: colors, dimens: dimens)
                Spacer(minLength: 5)
                SubscribeItems(colors: colors, dimens: dimens, model: model) {
                    onNavigationBack()
                }
                .frame(height: 280)
                Spacer(minLength: 15)
                Text(MR.strings().subscribe_conditions.localized())
                    .font(.system(size: 12))
                    .fontWeight(.light)
                Text(MR.strings().subscribe_terms.localized())
                    .font(.system(size: 7))
                    .padding(.bottom, 5)
            }
            .ignoresSafeArea(edges: .top)
            .preferredColorScheme(.dark)
            .statusBarStyle(.lightContent)
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .bottom)
            if (model.subscribeProcess) {
                Color.white.opacity(0.8)
                    .ignoresSafeArea()
                    .onTapGesture {
                        if (DebugManager().isDebug) {
                            withAnimation {
                                model.subscribeProcess.toggle()
                            }
                        }
                    }
                RadialGradient(colors: [colors.gradient1Start.toSColor(), colors.gradient1End.toSColor()], center: .center, startRadius: 20, endRadius: 50)
                    .frame(width: 170, height: 170)
                    .mask(ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: Color.black))
                        .scaleEffect(5))
                    .padding(.bottom, 200)
            }
        }
        .padding(.bottom, UIScreen.getSafeArea(side: .bottom))
        .background(Color.white)
        .ignoresSafeArea(edges: .bottom)
        
    }
}

struct SubscribeUIView_Previews: PreviewProvider {
    static var previews: some View {
        SubscribeUIView(source: "preview", onNavigationBack: {})
            .previewDevice(PreviewDevice(rawValue: "iphone x"))
    }
}
