//
//  DragListener.swift
//  iosApp
//
//  Created by rst10h on 4.07.22.
//

import Foundation
import UIKit
import Lottie
import SVGKit

class DragListener: UIPanGestureRecognizer {
    
    //Turn isWrapper = true if you are using a drag listener on the same view that will be dragged
    var isWrapper: Bool = true
    
    var lastPoint: CGPoint? = nil
    var prevTouchCount: Int = 0
    
    var onDrag : ((_ dx: CGFloat, _ dy: CGFloat) -> Void)? = nil
    
}

//todo: need to avoid code duplication!

extension InspUIView {
    
    func setOnDragListener(isWrapper: Bool = true, action :@escaping (_ dx: CGFloat, _ dy: CGFloat) -> Void) {
        let dragRecogniser = DragListener(target: self, action: #selector(onViewDragged(sender:)))
        dragRecogniser.isWrapper = isWrapper
        dragRecogniser.onDrag = action
        dragRecogniser.delegate = self
        dragRecogniser.maximumNumberOfTouches = 2
        self.isUserInteractionEnabled = true
        self.addGestureRecognizer(dragRecogniser)
    }
    
    @objc func onViewDragged(sender: DragListener) {
        
        if (sender.state == .began || sender.prevTouchCount != sender.numberOfTouches) {
            sender.lastPoint = sender.translation(in: self.superview)
            sender.prevTouchCount = sender.numberOfTouches
        } else {
            if let last = sender.lastPoint {
                let new = sender.translation(in: self.superview)
                let dx = new.x - last.x
                let dy = new.y - last.y
                if (sender.isWrapper) {
                    sender.lastPoint = CGPoint(x: new.x, y: new.y)
                } else {
                    sender.lastPoint = CGPoint(x: new.x, y: new.y)
                   //sender.lastPoint = CGPoint(x: new.x - dx, y: new.y - dy)
                }
                if let onDrag = sender.onDrag {
                    onDrag(dx, dy)
                }
            }
        }
        
    }
    
}

extension SVGKImageView: UIGestureRecognizerDelegate {
    
    func setOnDragListener(isWrapper: Bool = true, action :@escaping (_ dx: CGFloat, _ dy: CGFloat) -> Void) {
        let dragRecogniser = DragListener(target: self, action: #selector(onViewDragged(sender:)))
        dragRecogniser.isWrapper = isWrapper
        dragRecogniser.onDrag = action
        dragRecogniser.delegate = self
        dragRecogniser.maximumNumberOfTouches = 2
        self.isUserInteractionEnabled = true
        self.addGestureRecognizer(dragRecogniser)
    }
    
    @objc func onViewDragged(sender: DragListener) {
        
        if (sender.state == .began || sender.prevTouchCount != sender.numberOfTouches) {
            sender.lastPoint = sender.translation(in: self.superview)
            sender.prevTouchCount = sender.numberOfTouches
        } else {
            if let last = sender.lastPoint {
                let new = sender.translation(in: self.superview)
                let dx = new.x - last.x
                let dy = new.y - last.y
                if (sender.isWrapper) {
                    sender.lastPoint = CGPoint(x: new.x, y: new.y)
                } else {
                    sender.lastPoint = CGPoint(x: new.x, y: new.y)
                   //sender.lastPoint = CGPoint(x: new.x - dx, y: new.y - dy)
                }
                if let onDrag = sender.onDrag {
                    onDrag(dx, dy)
                }
            }
        }
        
    }
    
}

extension LottieAnimationView: UIGestureRecognizerDelegate {
    
    func setOnDragListener(isWrapper: Bool = true, action :@escaping (_ dx: CGFloat, _ dy: CGFloat) -> Void) {
        let dragRecogniser = DragListener(target: self, action: #selector(onViewDragged(sender:)))
        dragRecogniser.isWrapper = isWrapper
        dragRecogniser.onDrag = action
        dragRecogniser.delegate = self
        dragRecogniser.maximumNumberOfTouches = 2
        self.isUserInteractionEnabled = true
        self.addGestureRecognizer(dragRecogniser)
    }
    
    @objc func onViewDragged(sender: DragListener) {
        
        if (sender.state == .began || sender.prevTouchCount != sender.numberOfTouches) {
            sender.lastPoint = sender.translation(in: self.superview)
            sender.prevTouchCount = sender.numberOfTouches
        } else {
            if let last = sender.lastPoint {
                let new = sender.translation(in: self.superview)
                let dx = new.x - last.x
                let dy = new.y - last.y
                if (sender.isWrapper) {
                    sender.lastPoint = CGPoint(x: new.x, y: new.y)
                } else {
                    sender.lastPoint = CGPoint(x: new.x, y: new.y)
                   //sender.lastPoint = CGPoint(x: new.x - dx, y: new.y - dy)
                }
                if let onDrag = sender.onDrag {
                    onDrag(dx, dy)
                }
            }
        }
        
    }
    
}
