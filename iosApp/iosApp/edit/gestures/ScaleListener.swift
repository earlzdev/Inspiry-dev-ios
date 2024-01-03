//
//  ScaleListener.swift
//  iosApp
//
//  Created by rst10h on 4.07.22.
//

import Foundation
import UIKit

class ScaleListener: UIPinchGestureRecognizer {
    
    var lastScale: CGFloat? = nil

    var onScale : ((_ deltaScale: CGFloat) -> Void)? = nil
    }

extension InspUIView {
    
    func setOnScaleListener(action :@escaping (_ deltaScale: CGFloat) -> Void) {
        let scaleRecogniser = ScaleListener(target: self, action: #selector(onViewScaled(sender:)))
        scaleRecogniser.onScale = action
        scaleRecogniser.delegate = self
        self.isUserInteractionEnabled = true
        self.addGestureRecognizer(scaleRecogniser)
    }
     
    @objc func onViewScaled(sender: ScaleListener) {
               
        if let rec = (sender as? UIPinchGestureRecognizer) {
            let new = rec.scale
            
            if (rec.state == .began) {
                sender.lastScale = rec.scale
            } else {
                if let last = sender.lastScale {
                    let ds = new - last
                    sender.lastScale = new
                    if let onscale = sender.onScale {
                        onscale(ds)
                    }
                }
            }
          
            

        }
    }
}
