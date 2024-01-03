//
//  InstrumentMenuItem.swift
//  iosApp
//
//  Created by rst10h on 25.01.22.
//

import SwiftUI
import shared

struct InstrumentMenuItem: View {
    let width: CGFloat
    let height: CGFloat
    let icon: String
    let tabName: String
    let color: SwiftUI.Color
    let fontSize: CGFloat = 12

    var body: some View {
        VStack() {
            Spacer()
            CyborgImage(name: icon)
                .scaledToFill()
                .frame(width: width, height: height)
            Text(tabName)
                .font(.system(size: fontSize))
                .lineLimit(1)
                .foregroundColor(.white)
            Spacer()
        }
        .frame(minWidth: 60)
        .colorMultiply(color)
    }
}

struct InstrumentMenuItem_Preview: PreviewProvider {
    static var previews: some View {
        InstrumentMenuItem(width: 20, height: 20, icon: "ic_instrument_color", tabName: "Template", color: .black)
    }
}
