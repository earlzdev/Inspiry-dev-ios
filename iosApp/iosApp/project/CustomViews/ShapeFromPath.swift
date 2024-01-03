//
//  SwiftUIView.swift
//  iosApp
//
//  Created by rst10h on 24.06.22.
//

import SwiftUI

struct ShapeFromPath: Shape {
    let path: Path
    func path(in rect: CGRect) -> Path {
        return path
    }
}

struct SwiftUIView_Previews: PreviewProvider {
    static var previews: some View {
        ZStack {
            ShapeFromPath(path:
            Path { path in
                let width: CGFloat = 100
                let height: CGFloat = 100
                // 1
                path.move(
                    to: CGPoint(
                        x: 0 * width,
                        y: 1 * height
                    )
                )
                // 2
                path.addLine(
                    to: CGPoint(
                        x: 1 * width,
                        y: 1 * height)
                )
                // 3
                path.addLine(
                    to: CGPoint(
                        x: 0.5 * width,
                        y: 0 * height)
                )
                // 4
                path.closeSubpath()
                
            }
                          )
        }
        .frame(width: 100, height: 100)
    }
}
