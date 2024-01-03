//
//  OnBoardingTitle.swift
//  iosApp
//
//  Created by rst10h on 18.01.22.
//

import SwiftUI

struct OnBoardingTitle: View {
    let text: String
    let startColor: Color
    let endColor: Color
    let fontSize: CGFloat
    let fontWeight: Font.Weight
    init (_ text: String, startColor: Color, endColor: Color, fontSize: CGFloat = 25, fontWeight: Font.Weight = .semibold) {
        self.text = text
        self.startColor = startColor
        self.endColor = endColor
        self.fontSize = fontSize
        self.fontWeight = fontWeight
    }
    var body: some View {
        Text(text)
            .font(.system(size: fontSize))
            .fontWeight(fontWeight)
            .multilineTextAlignment(.center)
            .linearGradient(
                colors: [startColor, endColor],
                startPoint: .leading,
                endPoint: .trailing)
    }
}

struct OnBoardingTitle_Previews: PreviewProvider {
    static var previews: some View {
        OnBoardingTitle("OnBoarding text title",
                        startColor: Color.blue,
                        endColor: Color.green)
    }
}
