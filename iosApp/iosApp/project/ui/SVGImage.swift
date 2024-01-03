//
//  SVGImage.swift
//  MusicFeatureIos
//
//  Created by vlad on 7/4/21.
//

import Foundation
import SwiftUI
import Macaw


struct SVGImage: UIViewRepresentable {

    var svgName: String
    var contentMode: Macaw.MViewContentMode = Macaw.MViewContentMode.center

    func makeUIView(context: Context) -> SVGView {
        let svgView = SVGView()
        svgView.backgroundColor = UIColor(white: 1.0, alpha: 0.0)   // otherwise the background is black
        svgView.fileName = self.svgName
        svgView.contentMode = contentMode
        return svgView
    }

    func updateUIView(_ uiView: SVGView, context: Context) {
        uiView.fileName = self.svgName
    }

}

struct SVGImage_Previews: PreviewProvider {
    static var previews: some View {
        SVGImage(svgName: "ic_music_add").background(Color.black)
    }
}
