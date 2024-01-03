//
//  UnselectedTrackContent.swift
//  MusicFeatureIos
//
//  Created by vlad on 8/4/21.
//

import SwiftUI
import shared
import Kingfisher

struct UnselectedTrackContent: View {
    let colors: MusicColors
    let track: Track
    
    
    var body: some View {
        
        HStack {
            TrackImageView(image: track.image, colors: colors, isSelected: false)
            
            VStack {
                
                Text(track.title)
                    .lineLimit(1)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(colors.trackTextTitle.toSColor())
                    .frame(maxWidth: .infinity, alignment: .leading)
                
                Text(track.artist)
                    .lineLimit(1)
                    .font(.system(size: 10, weight: .regular))
                    .foregroundColor(colors.trackTextSubtitle.toSColor())
                    .frame(maxWidth: .infinity, alignment: .leading)
                
                
            }.frame(alignment: Alignment.leading)
            .padding(.leading, 9)
            .padding(.trailing, 5)
            .padding(.bottom, 2)
            
            
            Spacer()
        }
    }
}

struct UnselectedTrackContent_Previews: PreviewProvider {
    static var previews: some View {
        
        UnselectedTrackContent(colors: MusicDarkColors(), track: getTestTracks().last!)
            .environmentObject(MusicPlayerViewModel())
            .background(Color.black)
    }
}
