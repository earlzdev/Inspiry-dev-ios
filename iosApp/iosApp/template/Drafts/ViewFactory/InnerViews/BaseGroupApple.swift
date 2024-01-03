//
//  BaseGroupApple.swift
//  iosApp
//
//  Created by rst10h on 5.04.22.
//

import SwiftUI
import shared

struct BaseGroupApple: View {
    let children: [InspView<AnyObject>]
    @EnvironmentObject
    var templateModel: InspTemplateViewApple
    var body: some View {
        ZStack {
                EmptyView()
            }
        }
    
}
