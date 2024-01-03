//
//  KeyboardHelper.swift
//  iosApp
//
//  Created by rst10h on 25.07.22.
//

import UIKit
import Foundation

class KeyboardHeightHelper: ObservableObject {
    @Published
    var keyboardHeight: CGFloat = 0
    
    @Published
    var keyboardVisible: Bool = false
    
    init() {
        NotificationCenter.default.addObserver(forName: UIResponder.keyboardDidShowNotification,
                                               object: nil,
                                               queue: .main) { (notification) in
            guard let userInfo = notification.userInfo,
                  let keyboardRect = userInfo[UIResponder.keyboardFrameEndUserInfoKey] as? CGRect else { return }
            
            self.keyboardHeight = keyboardRect.height
            self.keyboardVisible = true
        }
        
        NotificationCenter.default.addObserver(forName: UIResponder.keyboardDidHideNotification,
                                               object: nil,
                                               queue: .main) { (notification) in
            self.keyboardHeight = 0
            self.keyboardVisible = false
        }
    }
}
