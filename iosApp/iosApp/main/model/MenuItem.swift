//
//  MenuIcon.swift
//  iosApp
//
//  Created by rst10h on 21.12.21.
//

import SwiftUI

class MenuItem: Hashable {
    
    var iconSize: CGFloat
    var text: String
    var icon: String
    var inactiveIcon: String?
    
    init(iconSize: CGFloat, text: String, icon: String, inactiveIcon: String?) {
        self.iconSize = iconSize
        self.icon = icon
        self.text = text
        self.inactiveIcon = inactiveIcon
    }
    
    static func == (lhs: MenuItem, rhs: MenuItem) -> Bool {
        return lhs.icon == rhs.icon && lhs.text == rhs.text
    }
    func hash(into hasher: inout Hasher) {
        hasher.combine(text)
        hasher.combine(icon)
    }
    
}
