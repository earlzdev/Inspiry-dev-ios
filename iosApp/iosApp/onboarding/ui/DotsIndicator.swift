//
//  DotsIndicator.swift
//  iosApp
//
//  Created by rst10h on 18.01.22.
//

import SwiftUI

struct DotsIndicator: View {
    let max: Int
    let current: Int
    let activeSize: CGFloat
    let inactiveSize: CGFloat
    let activeColor: Color
    let inactiveColor: Color
    
    init(_ current: Int = 2,
         max: Int = 4,
         activeSize: CGFloat = 8.4,
         inactiveSize: CGFloat = 7,
         activeColor: Color = Color.blue,
         inactiveColor: Color = Color.gray ) {
        
        self.current = current
        self.max = max
        self.activeSize = activeSize
        self.inactiveSize = inactiveSize
        self.activeColor = activeColor
        self.inactiveColor = inactiveColor
    }
    
    var body: some View {
        HStack(spacing: inactiveSize) {
            ForEach (0..<max, id: \.self) { index in
                let color = index == current ? activeColor : inactiveColor
                let size = index == current ? activeSize : inactiveSize
                Circle()
                    .fill(color)
                    .frame(width: size, height: size)
            }
        }
    }
}

struct DotsIndicator_Previews: PreviewProvider {
    static var previews: some View {
        DotsIndicator()
    }
}
