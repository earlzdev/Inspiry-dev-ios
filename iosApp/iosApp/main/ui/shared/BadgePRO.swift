//
//  BadgePRO.swift
//  iosApp
//
//  Created by rst10h on 10.01.22.
//

import SwiftUI
import Macaw

struct BadgePRO: View {
    let size: CGFloat
    init (_ size: CGFloat) {
        self.size = size
    }
    var body: some View {
        Text("PRO")
            .font(.system(size: size))
            .fontWeight(.bold)
            .kerning(0)
            .padding(.vertical, size * 0.3)
            .padding(.horizontal, size * 0.45)
            .foregroundColor(Color.white)
            .background(
                LinearGradient(
                    colors:[Color.fromInt(0x4E33F1), Color.fromInt(0x30CDFF), Color.fromInt(0xB0F1FF)],
                    startPoint: UnitPoint(x: 0.3, y: 1.3),
                    endPoint: UnitPoint(x: 0.7, y: -0.3)))
            .cornerRadius(size * 0.5)
        
    }
}

struct BadgePRO_Previews: PreviewProvider {
    static var previews: some View {
        VStack {
            BadgePRO(10)
            Image("ic_premium_template")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(height: 30)
                
            
        }
        .previewLayout(PreviewLayout.sizeThatFits)
        .padding()
    }
}
