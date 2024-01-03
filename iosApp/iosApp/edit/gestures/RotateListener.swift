//
//  RotateListener.swift
//  iosApp
//
//  Created by rst10h on 4.07.22.
//

import Foundation
import UIKit

class RotateListener: UIRotationGestureRecognizer {
    
    var lastRotate: CGFloat? = nil

    var onRotate : ((_ deltaRotation: CGFloat) -> Void)? = nil
    }

extension InspUIView {
    
    func setOnRotateListener(action :@escaping (_ deltaRotation: CGFloat) -> Void) {
        let rotateRecogniser = RotateListener(target: self, action: #selector(onViewRotated(sender:)))
        rotateRecogniser.onRotate = action
        rotateRecogniser.delegate = self
        self.isUserInteractionEnabled = true
        self.addGestureRecognizer(rotateRecogniser)
    }
     
    @objc func onViewRotated(sender: RotateListener) {
               
        if let rec = (sender as? UIRotationGestureRecognizer) {
            let new = rec.rotation
            
            if (sender.state == .began) {
                sender.lastRotate = rec.rotation
            } else {
                if let last = sender.lastRotate {
                    let ds = new - last
                    sender.lastRotate = new
                    if let onrotate = sender.onRotate {
                        onrotate(ds)
                    }
                }
            }
          
            

        }
    }
}
