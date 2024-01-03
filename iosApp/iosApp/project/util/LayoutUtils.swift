//
//  LayoutUtils.swift
//  iosApp
//
//  Created by rst10h on 6.04.22.
//

import SwiftUI
import shared

extension shared.Alignment {

    var unitPoint: UnitPoint {
        switch self {
        case .topStart: return .topLeading
        case .topCenter: return .top
        case .topEnd: return .topTrailing
        case .centerStart: return .leading
        case .centerEnd: return .trailing
        case .bottomStart: return .bottomLeading
        case .bottomCenter: return .bottom
        case .bottomEnd: return .bottomTrailing
        default: return .center
        }
    }
}

extension Media {
    func getAnchor() -> UnitPoint {
        return layoutPosition.alignBy.unitPoint
    }
}
