//
//  MediaViewUIController.swift
//  iosApp
//
//  Created by rst10h on 6.07.22.
//

import SwiftUI
import Kingfisher
import shared

struct MediaViewUI: UIViewRepresentable {
    
    weak var inspView: InspMediaView?
    
    weak var innerView: InnerMediaViewApple?
    
    init(inspView: InspMediaView) {
        self.inspView = inspView
        self.innerView = inspView.innerMediaView as? InnerMediaViewApple
    }
    
    func makeUIView(context: UIViewRepresentableContext<MediaViewUI>) -> UIView {
        if inspView?.userCanEdit() == true  {
            return innerView?.mediaFrame ?? UIView()
        } else {
            if  inspView?.isVideo() == true {
                return innerView?.playerView.innerPlayerView ?? UIView()
            } else {
                return innerView?.imageView ?? UIView()
            }
        }
    }
    
    func updateUIView(_ uiView: UIView, context: UIViewRepresentableContext<MediaViewUI>) {
        
    }
    


}


extension UIView {
    var parentViewController: UIViewController? {
        var parentResponder: UIResponder? = self.next
        while parentResponder != nil {
            if let viewController = parentResponder as? UIViewController {
                return viewController
            }
            parentResponder = parentResponder!.next
        }
        return nil
    }
}
