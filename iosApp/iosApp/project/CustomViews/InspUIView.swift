//
//  InspUIView.swift
//  iosApp
//
//  Created by rst10h on 11.07.22.
//

import Foundation
import UIKit

class InspUIView: UIView, UIGestureRecognizerDelegate {
    var onFrameUpdated: ((_ bounds: CGRect) -> Void)?
  
    override func layoutSubviews() {
        super.layoutSubviews()
        onFrameUpdated?(self.bounds)
    }
    
    func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool {
        return true
    }
}
