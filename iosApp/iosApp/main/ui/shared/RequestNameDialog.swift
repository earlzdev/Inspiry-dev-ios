//
//  RequestNameDialog.swift
//  iosApp
//
//  Created by rst10h on 12.01.22.
//

import SwiftUI
import shared

struct RequestNameDialog: View {
    let title: String
    let cancelAction: () -> Void
    let appruveAction: (String) -> Void
    @State var name: String
    
    init (title: String, name: String, cancelAction: @escaping () -> Void, appruveAction: @escaping (String) -> Void) {
        self.name = name
        self.title = title
        self.cancelAction = cancelAction
        self.appruveAction = appruveAction
    }
    var body: some View {
        VStack(alignment: .leading) {
            Text(title)
                .font(.system(size: 22))
                .foregroundColor(.black)
                .fontWeight(.semibold)
                .padding(.vertical)
                .padding(.horizontal, 25)
            TextField("Template name", text: $name)
                .lineLimit(1)
                .font(.system(size: 18))
                .foregroundColor(.black)
                .padding(.vertical, 10)
                .padding(.horizontal)
                .background(RoundedRectangle(cornerRadius: 8).fill(Color.fromInt(0xECECEC)))
                
                
            .padding(.horizontal)
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
                Button(action: { appruveAction(name) }, label: {
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
        .background(Color.white)
        .clipShape(RoundedRectangle(cornerRadius: 15))
        .frame(maxHeight: .infinity, alignment: .center)
        .padding(.horizontal, 25)
        .ignoresSafeArea()
        
    }
    
}

struct RequestNameDialog_Previews: PreviewProvider {
    static var previews: some View {
        RequestNameDialog(title: MR.strings().dialog_name_title.localized(),
                          name: "test",
                          cancelAction: {}, appruveAction: {_ in})
            .preferredColorScheme(.dark)
    }
}
