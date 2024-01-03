//
//  EditWrapperView2.swift
//  iosApp
//
//  Created by vlad on 17/1/22.
//

import SwiftUI
import shared
import os

struct EditWrapperView: View {
    
    @State
    var selectedViewId: Int = 0
    
    
    func printAllFiles(bundle: Bundle) {
        
        let files = try? FileManager.default.contentsOfDirectory(atPath: bundle.bundlePath)
        
        
        let logger = Logger(subsystem: "com.steipete.LoggingTest", category: "main")
        
        if files != nil {
            for file in files! {
                logger.info("Logging \(file)")
            }
        }
    }
    
    var body: some View {
        
        ZStack {
            
            Spacer().frame(maxWidth: .infinity, maxHeight: .infinity)
                .contentShape(Rectangle())
                .onTapGesture {
                    self.selectedViewId = -1
                }
            
            VStack {
                ChildView(color: Color.red, id: 1, selectedViewId: $selectedViewId)
                ChildView(color: Color.blue, id: 2, selectedViewId: $selectedViewId)
                ChildView(color: Color.yellow, id: 3, selectedViewId: $selectedViewId)
                ChildView(color: Color.pink, id: 4, selectedViewId: $selectedViewId)
                
                let mrImage = MR.images().notification_remove_bg
                
                let uiImage = mrImage.toUIImage()
                
                if uiImage != nil {
                    Image(uiImage: uiImage!)
                } else {
                    let _ = printAllFiles(bundle: mrImage.bundle)
                }
                
                Text(MR.strings().template_is_loading.localized(lang: "es")!)
            }
            
        }.backgroundPreferenceValue(MyPreferenceKey.self) { preferences in
            
            let selectedViewPref = preferences.first(where: { item in
                item.id == self.selectedViewId
            })
            
            GeometryReader { geo in
                
                if selectedViewPref != nil {
                    let topLeading: CGPoint = geo[selectedViewPref!.topLeading]
                    let bottomTrailing: CGPoint = geo[selectedViewPref!.bottomTrailing!]
                    
                    // TODO: make android like animation
                    RoundedRectangle(cornerRadius: 6)
                        .stroke(lineWidth: 2)
                        .foregroundColor(Color.gray)
                        .frame(width: bottomTrailing.x - topLeading.x, height: bottomTrailing.y - topLeading.y)
                        .offset(x: topLeading.x, y: topLeading.y)
                        .transition(.opacity.combined(with: .scale).animation(.easeOut))
                        .animation(.none)
                }
                
            }.frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        }
    }
}

struct MyPreferenceData: Equatable {
    let id: Int
    let topLeading: Anchor<CGPoint>
    var bottomTrailing: Anchor<CGPoint>? = nil
}

struct MyPreferenceKey: PreferenceKey {
    // or any custom Equatable type
    typealias Value = [MyPreferenceData]
    
    static var defaultValue: [MyPreferenceData] = []
    
    static func reduce(value: inout [MyPreferenceData], nextValue: () -> [MyPreferenceData]) {
        value.append(contentsOf: nextValue())
    }
}

struct ChildView: View {
    let color: SwiftUI.Color
    let id: Int
    
    @Binding
    var selectedViewId: Int
    
    var body: some View {
        color.opacity(0.4)
            .frame(width: 60, height: 60)
            .onTapGesture {
                withAnimation {
                    self.selectedViewId = id
                }
            }
            .anchorPreference(key: MyPreferenceKey.self, value: .topLeading, transform: { anchor in
                [MyPreferenceData(id: self.id, topLeading: anchor)]
            })
            .transformAnchorPreference(key: MyPreferenceKey.self, value: .bottomTrailing) { (value: inout [MyPreferenceData], anchor: Anchor<CGPoint>) in
                
                //value[0].bottomTrailing = anchor
                
                for (index, _) in value.enumerated() {
                    value[index].bottomTrailing = anchor
                }
            }
    }
}

struct EditWrapperView_Previews: PreviewProvider {
    static var previews: some View {
        EditWrapperView()
    }
}
