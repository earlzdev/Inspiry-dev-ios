//
//  PreviewViewModel.swift
//  iosApp
//
//  Created by vlad on 14/1/22.
//

import Foundation
import shared
import SwiftUI
import Combine
import os
import Toaster

class PreviewViewModel: ObservableObject {
    
    let colors: PreviewColors = PreviewColorsLight()
    let dimens: PreviewDimens = PreviewDimensPhone()
    
    @Published
    var instLayoutVisible: Bool
    
    @Published
    var frameProgress: CGFloat = 0 //0..1
    
    let settings: Settings
    
    //let publisher = PassthroughSubject<CGFloat, Never>()
    private var subscription: AnyCancellable? = nil
    
    private static let IG_LAYOUT_VISIBLE = "ig_layout_visible"
    private static let IG_LAYOUT_TOAST_SNOWN = "ig_layout_toast_shown"
    private static let logger = Logger(subsystem: Bundle.main.bundleIdentifier!, category: "preview")
    private static let LOOP_PREVIEW = true
    
    
    init(templateView: InspTemplateView) {
        
        settings = Dependencies.resolveAuto()
        _instLayoutVisible = Published(initialValue: settings.getBoolean(key: PreviewViewModel.IG_LAYOUT_VISIBLE, defaultValue: false))
        
        templateView.updateFramesListener = { [self] _ in
            DispatchQueue.main.async {
                frameProgress = templateView.currentFrame.cg / templateView.getDuration_().cg
            }
        }
    }
    
    func onLongPress() {
        withAnimation(.easeOut) {
            instLayoutVisible = !instLayoutVisible
        }
        settings.putBoolean(key: PreviewViewModel.IG_LAYOUT_VISIBLE, value: instLayoutVisible)
    }
    
    func mayShowToast() {
        if !settings.getBoolean(key: PreviewViewModel.IG_LAYOUT_TOAST_SNOWN, defaultValue: false) {
            Toast(text: MR.strings().preview_long_press_hint.localized(), duration: Delay.long).show()
            settings.putBoolean(key: PreviewViewModel.IG_LAYOUT_TOAST_SNOWN, value: true)
        }
        
    }
}
