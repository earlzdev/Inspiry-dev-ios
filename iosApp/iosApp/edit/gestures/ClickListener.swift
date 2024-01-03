//
//  ClickListener.swift
//  iosApp
//
//  Created by rst10h on 29.06.22.
//

import Foundation
import UIKit
import SVGKit

class ClickListener: UITapGestureRecognizer {
     var onClick : (() -> Void)? = nil
    }

extension UIView {
    
    func setOnClickListener(action :@escaping () -> Void){
        let tapRecogniser = ClickListener(target: self, action: #selector(onViewClicked(sender:)))
        tapRecogniser.onClick = action
        self.isUserInteractionEnabled = true
        self.addGestureRecognizer(tapRecogniser)
    }
    
    func removeOnClickListener() {
        if let tapGesture = self.gestureRecognizers?.first(where: { $0 is UITapGestureRecognizer }) {
            self.removeGestureRecognizer(tapGesture)
        } else {
            return
        }
    }
     
    @objc func onViewClicked(sender: ClickListener) {
        if let onClick = sender.onClick {
            onClick()
        }
    }
     
}
