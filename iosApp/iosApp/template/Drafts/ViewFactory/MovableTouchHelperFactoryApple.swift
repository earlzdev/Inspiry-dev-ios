//
//  MovableTouchHelperFactoryApple.swift
//  iosApp
//
//  Created by rst10h on 27.01.22.
//

import Foundation
import shared

class MovableTouchHelperFactoryApple: MovableTouchHelperFactory {
    func create(view: InspView<AnyObject>) -> MovableTouchHelper {
        return MovableTouchHelperApple(inspView: view, guideLine: GuideLineManagerApple())
    }
}
