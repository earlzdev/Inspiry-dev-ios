//
//  MutableStack.swift
//  iosApp
//
//  Created by rst10h on 22.06.22.
//

import Foundation
import SwiftUI

struct MutableStack<Content>: View where Content: View {
    private var orientation: Axis.Set?
    let content: Content
    
    init(orientation: Axis.Set?, @ViewBuilder content: () -> Content) {
        self.orientation = orientation
        self.content = content()
    }
    
    var body: some View {
        switch orientation {
        case Axis.Set.vertical:
            VStack(alignment: .center, spacing: 0) {
                content
            }
            
        case Axis.Set.horizontal:
            HStack(alignment: .center, spacing: 0) {
                content
            }
            
        default:
            ZStack(alignment: .center) {
                content
            }
        }
    }
}
