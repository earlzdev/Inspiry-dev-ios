//
//  TabItemWithIcon.swift
//  iosApp
//
//  Created by rst10h on 20.12.21.
//

import SwiftUI
import shared

struct TabItemWithIcon: View {
    let width: CGFloat
    let height: CGFloat
    let iconName: String
    let tabName: String
    let textColor: SwiftUI.Color

    let font = MR.fontsMade().bold.uiFont(withSize: 12)
    var body: some View {
        VStack {
            Spacer()
            CyborgImage(name: iconName)
                .scaledToFill()
                .frame(width: width, height: height)
            Text(tabName.uppercased())
                .font(Font(font as CTFont))
                .foregroundColor(textColor)
            Spacer()
        }.frame(minWidth: 60)
    }
}

struct TabIcon_Preview: PreviewProvider {
    static var previews: some View {
        TabItemWithIcon(width: 20, height: 20, iconName: "ic_pro_disabled", tabName: "Template", textColor: .black)
    }
}
