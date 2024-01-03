//
//  MyTemplateActions.swift
//  iosApp
//
//  Created by rst10h on 12.01.22.
//

import SwiftUI

struct MyTemplateActions: View {
    let action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 3) {
            Circle()
                .fill(Color.fromInt(0x979797))
                .frame(width: 4, height: 4)
            Circle()
                .fill(Color.fromInt(0x979797))
                .frame(width: 4, height: 4)
            Circle()
                .fill(Color.fromInt(0x979797))
                .frame(width: 4, height: 4)
        }
        }
        .frame(width: 26, height: 18)
        .background(Capsule().fill().foregroundColor(Color.white.opacity(0.9)))
        .shadow(color: Color.black.opacity(0.30), radius: 5, x: 0, y: 0)
    }
}

struct MyTemplateActions_Previews: PreviewProvider {
    static var previews: some View {
        MyTemplateActions() {}
    }
}
