//
//  StartView.swift
//  iosApp
//
//  Created by vlad on 13/7/21.
//

import SwiftUI
import shared
import Toaster

struct StartView: View {
    @State private var fullScreenSelection: String? = nil
    @State private var dialogSelection: String? = nil
    private static let TAG_FONTS_DIALOG = "FontsDialog"
    static let TAG_SUBSCRIBE_VIEW = "SubscribeView"
    @EnvironmentObject
    private var licenseManagerWrapper: LicenseManagerAppleWrapper
    
    private var leakTest = InnerMediaViewList()
    
    private func loadTemplatesAndPrint() {

        TemplateValidator.validateTemplates()
        TemplateValidator.validateTexts()
        TemplateValidator.validateStickers()
    }
    
    init() {
//        for i in 0...100 {
//            let inner1 = InnerMediaViewApple()
//            if (i > 0) {
//                inner1.setVideoInner(uri: "assets://template-resources/gradient/GradientBlackFriday/back.mp4", textureIndex: 0)
//            }
//            leakTest.leakTest.append(inner1)
//        }
//        leakTest.leakTest.append(inner2)
    }
    
    var body: some View {
        
          
            VStack {
                VStack(spacing:10) {
                    Button("Open Fonts Dialog") {
                        if dialogSelection != nil {
                            withAnimation(.easeIn(duration: 0.2)) {
                                dialogSelection = nil
                            }
                        } else {
                            withAnimation(.easeOut(duration: 0.35)) {
                                dialogSelection = StartView.TAG_FONTS_DIALOG
                            }
                        }
                    }

                    Button("Load templates and validate") {
                        DispatchQueue.global().async {
                            
                            loadTemplatesAndPrint()

                            DispatchQueue.main.sync {
                                Toast(text: "Finished loading templates. All templates are valid!").show()
                            }
                        }
                    }
                    
                    Button("memory leak test") {
                        DispatchQueue.global().async { [self] in
                            self.leakTest.leakTest.removeAll()
                            var a = self.leakTest.leakTest
                            print("remove aptempt")
//                            a.removeAll()
                            DispatchQueue.main.sync {
                                Toast(text: "memory leak test finished").show()
                            }
                        }
                    }
                    
                    NavigationLink(
                        destination: EditWrapperView(), tag: "EditWrapperView", selection: $fullScreenSelection) {
                        
                        Text("Open EditWrapperView")
                    }
                    
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(Color.white)
                .navigationBarHidden(true)
                
                if dialogSelection == StartView.TAG_FONTS_DIALOG {
                    let appearTransition = AnyTransition.opacity.combined(with: .move(edge: .bottom))
                       
//                    FontDialogView(fontData: nil, text: "Random", callbacks: EmptyFontDialogCallbacks())
//                        .transition(appearTransition)
                }
            }
        }
}

class InnerMediaViewList {
    var leakTest: [InnerMediaViewApple] = []
}

struct StartView_Previews: PreviewProvider {
    static var previews: some View {
        StartView()
    }
}
