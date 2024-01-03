//
//  SimpleVectorMask.swift
//  iosApp
//
//  Created by rst10h on 11.05.22.
//

import SwiftUI
import shared

struct SimpleVectorMask: View {
    let source: String = "assets://template-resources/love/LoveHeartMask/photo_mask.json"
    var body: some View {
        let asset = ResourceContainerExtKt.getAssetByFilePath(MR.assets(), filePath: source.removeSheme())
        ZStack{
            Image("testMask")
                .mask(
                    ZStack {
                        Text("1234")
                            .font(.system(size: 100))
                        LottieView(name: asset.fileName, isPlaying: true, bundle: asset.bundle)
                            .blendMode(.destinationOut)
                    }
                        .compositingGroup()
                    //                    LottieView(name: asset.fileName, isPlaying: true, bundle: asset.bundle)
                    //                .blendMode(.luminosity)
                )
        }
        .background(Color.gray)
    }
}

struct SimpleVectorMask_Previews: PreviewProvider {
    static var previews: some View {
        //        SimpleVectorMask(source: "assets://template-resources/love/LoveHeartMask/photo_mask.json")
        //            .preferredColorScheme(.dark)
        SimpleVectorMask()
        
    }
}
