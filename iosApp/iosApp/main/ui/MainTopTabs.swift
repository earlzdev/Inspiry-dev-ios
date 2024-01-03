//
//  TopTabsWithIcon.swift
//  iosApp
//
//  Created by rst10h on 21.12.21.
//

import SwiftUI
import shared

struct MainTopTabs: View {
    let menuItems: [MenuItem]
    let colors: TopTabColors
    let activePage: MainScreenPages
    let onTabSelected: (MainScreenPages) -> ()

    var body: some View {
        
        HStack {
            Spacer()
            ForEach(menuItems.indices, id: \.self) {index in
                let item = menuItems[index]
                let color = activePage.ordinal == index ? colors.textActive : colors.textInactive
                let icon = activePage.ordinal == index ? item.icon : item.inactiveIcon
                TabItemWithIcon(width: item.iconSize, height: item.iconSize, iconName: icon ?? item.icon, tabName: item.text, textColor: color.toSColor())
                    .onTapGesture {
                        let nextPage = MainScreenPages.values().get(index: Int32(index))!
                        onTabSelected(nextPage)
                    }
                Spacer()
            }
        }
        .padding(.horizontal, 30)
    }
}

struct TopBar_preview: PreviewProvider {
    static var previews: some View {
        
        let menu = [
            MenuItem(iconSize: 20, text: "First", icon: "ic_templates_enabled", inactiveIcon: "ic_templates_disabled"),
        MenuItem(iconSize: 20, text: "Second", icon:  "ic_stories_enabled", inactiveIcon: "ic_stories_disabled"),
            MenuItem(iconSize: 20, text: "Third", icon: "ic_pro_enabled", inactiveIcon: "ic_pro_disabled"),
            MenuItem(iconSize: 20, text: "Fourth", icon: "ic_pro_enabled", inactiveIcon: "ic_pro_disabled")
        ]
        MainTopTabs(menuItems: menu, colors: TopTabColorsLight(), activePage: .templates, onTabSelected: {_ in })
    }
}

