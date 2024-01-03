//
//  DragRelocationDelegate.swift
//  iosApp
//
//  Created by rst10h on 5.09.22.
//

import Foundation
import SwiftUI

struct DragRelocateDelegate: DropDelegate {
    
    let item: String
    
    @Binding var listData: [String]
    @Binding var current: String?
    
    
    func dropEntered(info: DropInfo) {
        guard let current = current else { return }
        if item != current {
            let from = listData.firstIndex(of: current)!
            let to = listData.firstIndex(of: item)!
            if listData[to] != current {
                listData.move(fromOffsets: IndexSet(integer: from),
                              toOffset: to > from ? to + 1 : to)
            }
        }
    }
    
    func validateDrop(info: DropInfo) -> Bool {
        
        return true
    }
    
    func dropUpdated(info: DropInfo) -> DropProposal? {
        return DropProposal(operation: .move)
    }
    
    func dropExited(info: DropInfo) {
        
    }
    
    
    
    func performDrop(info: DropInfo) -> Bool {
        self.current = nil
        return true
    }
}
