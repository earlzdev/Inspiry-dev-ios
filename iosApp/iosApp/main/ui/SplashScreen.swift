//
//  SplashScreen.swift
//  iosApp
//
//  Created by rst10h on 18.11.22.
//

import SwiftUI

struct SplashScreen: View {
    var body: some View {
        Image("splashScreenLogo")
            .resizable()
            .aspectRatio(contentMode: .fit)
            .padding(120)
    }
}

struct SplashScreen_Previews: PreviewProvider {
    static var previews: some View {
        SplashScreen()
    }
}
