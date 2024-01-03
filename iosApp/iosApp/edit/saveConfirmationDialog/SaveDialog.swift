//
//  SaveDialog.swift
//  iosApp
//
//  Created by rst10h on 15.08.22.
//

import SwiftUI
import shared

struct SaveDialog: View {
    @Binding var isVisible: Bool
    let positiveAction: () -> Void
    let negativeAction: () -> Void
    private let colors = MainScreenColorsLight()
    var body: some View {
        ZStack(alignment: .center) {
            VStack(alignment: .leading) {
                Text(MR.strings().save_project_title.localized())
                    .foregroundColor(.black)
                    .font(.system(size: 20))
                    .fontWeight(.bold)
                    .padding(.bottom, 45)
                VStack(alignment: .trailing, spacing: 0) {
                    Button(action: negativeAction, label: {
                            Text(MR.strings().save_project_negative.localized())
                                .foregroundColor(colors.instagramLinkTextColor.toSColor())
                                .font(.system(size: 17))
                                .fontWeight(.bold)
                                .textCase(.uppercase)
                        }
                    )
                    Spacer()
                        .frame(height: 30.cg)
                    Button(action: positiveAction, label: {
                        Text(MR.strings().save_project_positive.localized())
                            .foregroundColor(colors.instagramLinkTextColor.toSColor())
                            .font(.system(size: 17))
                            .fontWeight(.bold)
                            .textCase(.uppercase)
                    })
                    
                }
                .frame(maxWidth: .infinity, alignment: .trailing)
            }
            .padding(.horizontal, 23)
            .padding(.bottom, 30)
            .padding(.top, 40)
            .background(Color.white)
            .cornerRadius(15.cg)
            .padding()
        }
        .frame(maxHeight: .infinity, alignment: .center)
        .background(Color.black.opacity(0.4).onTapGesture {
            withAnimation {
                isVisible = false
            }
        })
        .ignoresSafeArea()
    }
}

struct SaveDialog_Previews: PreviewProvider {
    @State static var isVisible = true
    static var previews: some View {
        SaveDialog(isVisible: $isVisible, positiveAction: {}, negativeAction: {})
            .preferredColorScheme(.light)
    }
}
