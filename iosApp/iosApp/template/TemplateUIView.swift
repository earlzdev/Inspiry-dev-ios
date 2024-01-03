//
//  TemplteUIView.swift
//  iosApp
//
//  Created by vlad on 2/11/21.
//

import SwiftUI
import shared

struct TemplateUIView: View {
    @ObservedObject
    var templateModel: InspTemplateViewApple
    
    let infoViewColors: InfoViewColors = InfoViewColorsDark()
    let displayInfoText: Bool = true
    let canDisplayProgress: Bool = true
    let autoplay: Bool
    let playSound: Bool
    
    @ObservedObject
    var viewModel: InfoViewModelApple
    
    init(templateModel: InspTemplateViewApple, autoplay: Bool, playSound: Bool) {
        self._templateModel = ObservedObject(wrappedValue: templateModel)
        self._viewModel = ObservedObject(wrappedValue: InfoViewModelApple(coreModel: templateModel.infoViewModel))
        self.autoplay = autoplay
        self.playSound = playSound
    }
    
    var body: some View {
        
        ZStack() {
            if canDisplayProgress && viewModel.state is InspResponseLoading<KotlinUnit> {
                let stateValue = (viewModel.state as! InspResponseLoading<KotlinUnit>).progress
                
                InfoProgressView(infoViewColors: infoViewColors, displayInfoText: displayInfoText, stateValue: stateValue, cancelLoadImages: {})
                    .aspectRatio(
                        CGFloat( templateModel.templateFormat().aspectRatio() ), contentMode: ContentMode.fit)
                    .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
                
            } else if canDisplayProgress && viewModel.state is InspResponseError<KotlinUnit> {
                
                let error = (viewModel.state as! InspResponseError<KotlinUnit>).throwable
                InfoErrorView(infoViewColors: infoViewColors, displayInfoText: displayInfoText, reloadTemplate: {}, error: error)
                    .aspectRatio(
                        CGFloat( templateModel.templateFormat().aspectRatio() ), contentMode: ContentMode.fit)
                    .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
                
                
            } else {
                InnerTemplateView()
                    .clipped()
                    .environmentObject(templateModel)
            }
        }
        .onSizeChange {
                templateModel.onTemplateSizeChanged(newSize: $0)
                //print("template size changed \($0) \(templateModel.debugName)")
        }
        .onDisappear {
            templateModel.templateVisible = false
            if (templateModel.logLevel > 1) {
                print("disappear template \(templateModel.debugName)")
            }
        }
        .onAppear {
            templateModel.templateVisible = true
            if (templateModel.logLevel > 1) {
                print("appear template \(templateModel.debugName)")
            }
            if (autoplay) {
                templateModel.isMusicEnabled = playSound
                templateModel.startPlaying(resetFrame: false, mayPlayMusic: playSound)
            }
        }
    }
}

//struct TemplteUIView_Previews: PreviewProvider {
//    static var previews: some View {
//
//        TemplateUIView(template: getTestTemplate())
//    }
//}
