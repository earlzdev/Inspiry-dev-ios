//
//  PathPreviewTests.swift
//  iosApp
//
//  Created by rst10h on 31.10.22.
//

import SwiftUI

struct PathPreviewTests: View {
    let p = ApplePath()
    
    init() {
        p.setPathCornerRadius(absoluteRadius: 10)
        p.moveTo(x: 0, y: 9.3)
        p.lineTo(x: 0, y: 88.2)
        p.lineTo(x: 141.0, y: 88.2)
        p.lineTo(x: 141.0, y: 9.8)
        p.lineTo(x: 0, y: 9.8)
        p.close()
    }
    
    var body: some View {
        VStack{
            p.path
                .stroke(lineWidth: p.strokeWidth.cg)
                .fill(p.color)
                .drawingGroup()
                .clipped()
                .frame(width: 200, height: 100)
            Text("asd")
        }

    }
}

struct PathPreviewTests_Previews: PreviewProvider {
    static var previews: some View {
        PathPreviewTests()
    }
}
