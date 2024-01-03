//
//  MusicToolbarView.swift
//  MusicFeatureIos
//
//  Created by vlad on 7/4/21.
//

import SwiftUI
import shared
import Macaw

struct MusicToolbarView: View {
    let currentItem: MusicTab
    let colors: MusicColors
    let showItunesTab: Bool
    let onCurrentItemChange: (MusicTab) -> ()
    let onExit: () -> ()
    
    var body: some View {

        HStack {
            Button(action: onExit, label: {
                SVGImage(svgName: "ic_music_back")
                    .padding(.leading, 10)
                    .frame(width: 40, height: 50, alignment: .center)
            })
                
            HStack {
                if (showItunesTab) {
            
                    let text = MR.strings.init().music_tab_preview.localized()
                    
                    Button(action: { onCurrentItemChange(MusicTab.itunes) }, label: {
                        TabTextView(text: text, isSelected: currentItem == MusicTab.itunes, colors: colors)
                    })
                }
                Button(action: { onCurrentItemChange(MusicTab.library) }, label: {
                    TabTextView(text: MR.strings.init().music_tab_library.localized(), isSelected: currentItem == MusicTab.library, colors: colors)
                })
           
                Button(action: { onCurrentItemChange(MusicTab.myMusic) }, label: {
                    TabTextView(text: MR.strings.init().music_tab_my.localized(), isSelected: currentItem == MusicTab.myMusic, colors: colors)
                })
            
            }.frame(maxWidth: .infinity)
            

            Spacer().frame(width: 50, height: 50)
            
        }.frame(alignment: .top)
    }
}

struct MusicToolbarView_Previews: PreviewProvider {
    static var previews: some View {
        MusicToolbarView(currentItem: MusicTab.myMusic, colors: MusicDarkColors(), showItunesTab: false, onCurrentItemChange: {_ in } , onExit: { })
    }
}
