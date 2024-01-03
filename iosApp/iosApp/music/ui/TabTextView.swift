//
// Created by vlad on 7/4/21.
//

import SwiftUI
import shared

struct TabTextView: View {
    let text: String
    let isSelected: Bool
    let colors: MusicColors

    var body: some View {
        Text(text)
        .font(.system(size: 16, weight: isSelected ? .medium : .regular, design: .default))
        .lineLimit(1)
        .frame(maxWidth: .infinity, alignment: .center)
        .padding(.vertical, 15)
            .foregroundColor(isSelected ? colors.tabTextActive.toSColor() : colors.tabTextInactive.toSColor())
    }
}

struct TabTextView_Previews: PreviewProvider {
    static var previews: some View {
        TabTextView(text: "My Music", isSelected: false, colors: MusicDarkColors())
    }
}
