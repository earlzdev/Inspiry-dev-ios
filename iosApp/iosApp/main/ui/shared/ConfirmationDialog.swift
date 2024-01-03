//
//  ConfirmationDialog.swift
//  iosApp
//
//  Created by rst10h on 12.01.22.
//

import SwiftUI
import shared

struct ConfirmationDialog: View {
    let title: String
    let message: String
    let cancelAction: () -> Void
    let appruveAction: () -> Void
    var body: some View {
        VStack(alignment: .leading) {
            Text(title)
                .font(.system(size: 22))
                .foregroundColor(.black)
                .fontWeight(.semibold)
                .padding(.vertical)
                .padding(.horizontal, 25)
            Text(message)
                .font(.system(size: 18))
                .foregroundColor(.black)
                .padding(.horizontal, 25)
            HStack(spacing: 0) {
                Spacer()
                Button(action:
                    cancelAction, label: {
                        Text(MR.strings().cancel.localized())
                        .foregroundColor(Color.fromInt(0x5161F6))
                            .font(.system(size: 16))
                            .fontWeight(.bold)
                            .textCase(.uppercase)
                            .padding(.trailing, 20)
                    }
                )
                Button(action: appruveAction, label: {
                    Text(MR.strings().dialog_alert_confirm.localized())
                        .foregroundColor(Color.fromInt(0x5161F6))
                        .font(.system(size: 16))
                        .fontWeight(.bold)
                        .textCase(.uppercase)
                })
                
            }
            .padding()
            .padding(.horizontal)
            
        }
        .background(Color.fromInt(0xECECEC))
        .clipShape(RoundedRectangle(cornerRadius: 15))
        .frame(maxHeight: .infinity, alignment: .center)
        .padding(.horizontal, 25)
        .ignoresSafeArea()
        
    }
    
}

struct ConfirmationDialog_Previews: PreviewProvider {
    static var previews: some View {
        ConfirmationDialog(title: MR.strings().context_delete_title.localized(), message: MR.strings().context_delete_message.localized(), cancelAction: {}) {
        }
        .preferredColorScheme(.dark)
    }
}
