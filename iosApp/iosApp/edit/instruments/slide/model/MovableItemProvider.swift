//
//  MovableItemProvider.swift
//  iosApp
//
//  Created by rst10h on 6.09.22.
//

import Foundation

class MovableItemProvider: NSItemProvider {

     var onFinishedAction: (() -> Void)? = nil
        
    deinit {
        onFinishedAction?()
    }
}
