//
//  CommonClipboardManagerImpl.swift
//  iosApp
//
//  Created by vlad on 10/12/21.
//

import Foundation
import shared
import UIKit

class CommonClipboardManagerImpl: CommonClipBoardManager {
    
    func doCopyToClipboard(text: String) {
        let pasteBoard = UIPasteboard.general
        pasteBoard.string = text
    }
}
