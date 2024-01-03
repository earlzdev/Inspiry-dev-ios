//
//  RoundedCorner.swift
//  iosApp
//
//  Created by vlad on 6/11/21.
//

import SwiftUI
import shared

struct RoundedCorner: Shape {
    
    var radius: CGFloat = .infinity
    var corners: UIRectCorner = .allCorners
    
    func path(in rect: CGRect) -> Path {
        let path = UIBezierPath(roundedRect: rect, byRoundingCorners: corners, cornerRadii: CGSize(width: radius, height: radius))
        return Path(path.cgPath)
    }
}

extension View {
    func cornerRadius(_ radius: CGFloat, corners: UIRectCorner) -> some View {
        clipShape( RoundedCorner(radius: radius, corners: corners) )
    }
}

extension CornerRadiusPosition {
    var corners: UIRectCorner {
        get {
            switch self {
            case .onlyBottom:
                return [UIRectCorner.bottomLeft, UIRectCorner.bottomRight]
            
            case .onlyTop:
                return [UIRectCorner.topLeft, UIRectCorner.topRight]
            
            case .onlyLeft:
                return [UIRectCorner.topLeft, UIRectCorner.bottomLeft]
            
            case .onlyRight:
                return [UIRectCorner.bottomRight, UIRectCorner.topRight]
            
            default:
                return .allCorners
            }
        }
    }
}
