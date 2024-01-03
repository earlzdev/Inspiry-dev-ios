//
//  ToastManagerImpl.swift
//  iosApp
//
//  Created by vlad on 10/12/21.
//

import Foundation
import Toaster
import shared

class ToastManagerImpl: ToastManager {
    func displayToast(text: String, length: ToastLength) {
        Toast(text: text, duration: length == ToastLength.short_ ? Delay.short : Delay.long).show()
    }
}
