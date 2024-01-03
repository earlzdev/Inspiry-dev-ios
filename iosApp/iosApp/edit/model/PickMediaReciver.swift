//
//  PickImageReciver.swift
//  iosApp
//
//  Created by rst10h on 29.06.22.
//

import Foundation
import shared

protocol PickMediaReciver: ObservableObject {
    
    var mediaResult: [PickMediaResult] {get set}
    
    var galleryIsShown: Bool {get set}
}
