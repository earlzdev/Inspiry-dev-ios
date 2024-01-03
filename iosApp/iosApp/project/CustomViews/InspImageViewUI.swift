//
//  InspImageViewUI.swift
//  iosApp
//
//  Created by rst10h on 14.04.22.
//

import Foundation
import SwiftUI

class InspImageViewUI: UIImageView {
    override var image: UIImage? {
        didSet {
            guard let image = image else { return }
            
        }
    }
}
