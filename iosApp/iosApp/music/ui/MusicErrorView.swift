//
//  MusicErrorView.swift
//  MusicFeatureIos
//
//  Created by vlad on 13/4/21.
//

import SwiftUI
import shared

struct MusicErrorView: View {
    let colors: MusicColors
    let errorMessage: String?
    let onRetryClick: () -> ()
    
    var body: some View {
        VStack(alignment: .center, spacing: 0) {
            
            Text(MR.strings.init().music_error_button.localized())
                .lineLimit(1)
                .font(.system(size: 14, weight: .regular))
                .foregroundColor(colors.albumTextInactive.toSColor())
                .padding(.horizontal, 18)
                .padding(.vertical, 7)
                .background(colors.errorButtonBg.toSColor())
                .cornerRadius(8)
                .onTapGesture {
                    onRetryClick()
                }
            
            Text(MR.strings.init().music_error_message.localized() + ": \(String(describing: errorMessage))")
                .lineLimit(4)
                .font(.system(size: 13, weight: .regular))
                .foregroundColor(colors.trackTextTitle.toSColor())
                .padding(.horizontal, 20)
                .padding(.top, 6)
            
        }
    }
}

struct MusicErrorView_Previews: PreviewProvider {
    static var previews: some View {
        MusicErrorView(colors: MusicDarkColors(), errorMessage: "BadException lalalala", onRetryClick: { })
    }
}
