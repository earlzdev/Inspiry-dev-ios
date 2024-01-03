//
//  CyborgImage.swift
//  iosApp
//
//  Created by vlad on 8/11/21.
//

import Foundation
import Cyborg
import SwiftUI
import Macaw



struct CyborgImage: UIViewRepresentable {
    
    var name: String
    var useNaturalSize: Bool = true
    
    func makeUIView(context: Context) -> VectorView {
        let vectorView = VectorView(theme: theme, resources: resources)
        let drawable = VectorDrawable.named(name)
        vectorView.drawable = drawable
        if drawable != nil && useNaturalSize {
            vectorView.frame.size.height = drawable!.viewPortHeight
            vectorView.frame.size.width = drawable!.viewPortWidth
        }
        return vectorView
    }
    
    func updateUIView(_ uiView: VectorView, context: Context) {
        uiView.drawable = VectorDrawable.named(name)
    }
}

fileprivate let resources = Resources()
fileprivate let theme = Theme()

extension VectorDrawable {
    public static func named(_ name: String) -> VectorDrawable? {
        
        return Bundle.main.url(forResource: name, withExtension: "xml").flatMap { url in
            switch VectorDrawable.create(from: url) {
            case .ok(let drawable):
                return drawable
            case .error(let error):
                //throw InspError.RuntimeError("Could not create a vectordrawable named \(name); the error was \(error)")
                return nil
            }
        }
    }
}

struct CyborgImage_Previews: PreviewProvider {
    static var previews: some View {
        CyborgImage(name: "ic_edit_static").background(Color.black)
    }
}
