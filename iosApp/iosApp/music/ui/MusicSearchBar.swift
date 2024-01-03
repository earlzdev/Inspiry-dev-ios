//
//  MusicSearchBar.swift
//  MusicFeatureIos
//
//  Created by vlad on 13/4/21.
//

import SwiftUI
import shared

struct MusicSearchBar: View {
    let colors: MusicColors
    var searchQuery: Binding<String>
    
    var body: some View {
        
        let isBlank = searchQuery.wrappedValue.isEmpty
        
        HStack(spacing: 0) {
            SVGImage(svgName: "ic_music_search")
                .frame(width: 20, height: 30, alignment: .center)
                .padding(.leading, 10)
                .padding(.trailing, 5)
            
            TextField(MR.strings.init().music_search_hint.localized(),
                      text: searchQuery).lineLimit(1)
                .accentColor(colors.searchEditCursor.toSColor())
                
                .foregroundColor(isBlank ? colors.searchTextInactive.toSColor() : colors.searchTextActive.toSColor())
                .font(.system(size: 16, weight: isBlank ? .light : .regular))
            
            Spacer()
            
        }.frame(height: 30, alignment: .center)
        .background(colors.searchBg.toSColor())
        .cornerRadius(10)
        .padding(EdgeInsets(top: 10, leading: 17, bottom: 10, trailing: 17))
        
    }
}

struct MusicSearchBar_Previews: PreviewProvider {
    @State
    static var q = ""
    
    static var previews: some View {
        
        MusicSearchBar(colors: MusicDarkColors(), searchQuery: $q)
    }
}
