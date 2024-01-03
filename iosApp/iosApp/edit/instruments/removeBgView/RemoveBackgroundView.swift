//
//  RemoveBackgroundView.swift
//  iosApp
//
//  Created by rst10h on 29.08.22.
//

import SwiftUI
import shared

struct RemoveBackgroundView: View {
    let colors = RemovingBgColorsBlack()
    let dimens = RemovingBgDimensPhone()
    let onCancel: () -> ()
    var body: some View {
        ZStack(alignment: .center) {
            VStack {
                Spacer()
                LottieView(name: "app-resources+remove_background_progress", isPlaying: true, loopMode: .loop)
                    .frame(width: dimens.animationSize.cg, height: dimens.animationSize.cg, alignment: .center)
                    .padding(10.cg)
                Text("REMOVING\nBACKGROUND")
                    .font(.system(size: dimens.animatingTextSize.cg))
                    .fontWeight(.bold)
                    .multilineTextAlignment(.center)
                    .linearGradient(colors: [colors.removingBgGradientStart.toSColor(), colors.removingBgGradientEnd.toSColor()], startPoint: .leading, endPoint: .trailing)
                    .padding()
                
                Link(destination: URL(string: "https://apps.apple.com/app/apple-store/id1455009060?pt=120355336&ct=api-")!, label: { Image("photoroom_attribution")
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                    .frame(width: dimens.attributionWidth.cg, height: dimens.attributionHeight.cg) }
                )
                
                .padding(.top, 70.cg)
                
                
                Button(action: onCancel, label: {
                    HStack {
                        Text(MR.strings().cancel.localized())
                            .foregroundColor(.white)
                            .padding(5)
                        CyborgImage(name: "ic_subscribe_close")
                            .frame(width: 3, height: 3)
                            .frame(width: 25, height: 25)
                            .background(Circle().fill(Color.gray))
                        
                    }
                    .padding(.bottom, 20)
                }).buttonStyle(.plain)
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
        .background(colors.background.toSColor())
        .ignoresSafeArea()
    }
}

struct RemoveBackgroundView_Previews: PreviewProvider {
    static var previews: some View {
        RemoveBackgroundView() {}
    }
}
