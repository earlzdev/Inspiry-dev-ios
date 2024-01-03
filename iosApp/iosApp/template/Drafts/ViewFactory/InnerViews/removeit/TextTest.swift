//
//  TextTest.swift
//  iosApp
//
//  Created by rst10h on 25.04.22.
//

import SwiftUI

struct TextTest: View {
    var body: some View {
        ZStack {
            ZStack {
                UILabelTest()
                    .frame(width: 80, height: 30)
                    .offset(y: 50)
            }
            .frame(width: 100, height: 100)
            .background(Color.green)
        
        }
        .frame(width: 200, height: 200)
        .background(Color.blue)
    }
        
}

struct TextTest_Previews: PreviewProvider {
    static var previews: some View {
        TextTest()
    }
}
