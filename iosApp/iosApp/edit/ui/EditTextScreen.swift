//
//  TextEditor.swift
//  iosApp
//
//  Created by rst10h on 25.07.22.
//

import SwiftUI
import shared

struct EditTextScreen: View {
    let colors: EditColors
    let dimens: EditDimens
    
    @State
    var text: String
    
    let font: UIFont
    let onCancel: () -> Void
    let onDone: (String) -> Void
    
    @ObservedObject
    var keyboardHelper = KeyboardHeightHelper()
    
    var body: some View {
        VStack {
            EditTextTopBar(colors: colors, dimens: dimens, onBack: onCancel, onDone: { onDone(text)})
                .padding(.top, UIScreen.getSafeArea(side: .top))
                .background(colors.keyboardDoneBg.toSColor())
            MultilineTextEditor(text: $text, keyboardVisible: $keyboardHelper.keyboardVisible)
                .padding(.bottom, keyboardHelper.keyboardHeight)
            
        }
        .background(colors.editTextBack.toSColor())
        

    }
}

struct TextEditor_Previews: PreviewProvider {
    static let colors: EditColors = EditColorsLight()
    static let dimens: EditDimens = EditDimensPhone()
    
    static var previews: some View {
        EditTextScreen(
            colors: colors,
            dimens: dimens,
            text: "test string",
            font: UIFont.systemFont(ofSize: 40),
            onCancel: {},
            onDone: {_ in }

        )
    }
}
