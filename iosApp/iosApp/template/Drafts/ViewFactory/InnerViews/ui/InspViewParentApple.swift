//
//  InspViewParentApple.swift
//  iosApp
//
//  Created by rst10h on 15.04.22.
//

import SwiftUI
import shared
import Lottie

struct InspViewParentApple: View {
    @EnvironmentObject
    var templateParent: InspTemplateViewApple
    
    weak var inspParent: InspParent?
    
    var body: some View {
        MutableStack(orientation: (inspParent as? InspGroupView)?.media.orientation.toAxisSet()) {
            ForEach(templateParent.getChildren(inspParent: inspParent), id: \.self) { inspView in
                InspViewUI(inspView: inspView)
                    .environmentObject(templateParent)
            }
        }
    }
}

extension GroupOrientation {
    func toAxisSet() -> Axis.Set? {
        switch(self) {
        case .h: return Axis.Set.horizontal
        case .v: return Axis.Set.vertical
        default: return nil
        }
    }
}
