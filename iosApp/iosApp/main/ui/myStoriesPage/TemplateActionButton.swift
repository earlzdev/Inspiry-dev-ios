//
//  TemplateActionButton.swift
//  iosApp
//
//  Created by rst10h on 12.01.22.
//

import SwiftUI

struct TemplateActionButton: View {
    let text: String
    let action: () -> Void
    
    init (_ text: String, action: @escaping () -> Void) {
        self.text = text
        self.action = action
    }
    
    var body: some View {
        Button (action: action) {
            Text( text )
                .font(.system(size: 18))
                .foregroundColor(Color.fromInt(0x333333))
        }
        .frame(width: 300, height: 40, alignment: .center)
        .background(Capsule().fill().foregroundColor(Color.white))
    }
        
}

struct TemplateActionButton_Previews: PreviewProvider {
    static var previews: some View {
        TemplateActionButton("Button") {}
        .preferredColorScheme(.dark)
    }
}
