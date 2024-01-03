//
//  TemplatesActionDialog.swift
//  iosApp
//
//  Created by rst10h on 12.01.22.
//

import SwiftUI
import shared

struct TemplatesActionDialog: View {
    let templateName: String
    let action: (MyTemplatesActions, String?) -> Void
    @State var removeConfirmVisible = false
    @State var enterNameDialogVisible = false
    
    var body: some View {
        ZStack {
            if (removeConfirmVisible) {
                ConfirmationDialog(title: MR.strings().context_delete_title.localized(), message: MR.strings().context_delete_message.localized(), cancelAction: {
                    withAnimation {
                        removeConfirmVisible.toggle()
                    }
                }) {
                    withAnimation {
                        removeConfirmVisible.toggle()
                        action(.remove, nil)
                    }
                }
            }
            if (enterNameDialogVisible) {
                RequestNameDialog(title: MR.strings().dialog_name_title.localized(),
                                  name: templateName,
                                  cancelAction: {
                    withAnimation {
                        enterNameDialogVisible.toggle()
                    }
                }) { name in
                    enterNameDialogVisible.toggle()
                    action(.rename, name)
                }
            }
            if (!removeConfirmVisible && !enterNameDialogVisible){
                HStack {
                    Spacer()
                    VStack(spacing: 10) {
                        TemplateActionButton(MR.strings().copy_action.localized()) {
                            action(.copy, nil)
                        }
                        TemplateActionButton(MR.strings().rename.localized()) {
                            withAnimation {
                                enterNameDialogVisible.toggle()
                            }
                        }
                        TemplateActionButton(MR.strings().delete_.localized()) {
                            withAnimation {
                                removeConfirmVisible.toggle()
                            }
                        }
                    }
                    Spacer()
                }
            }
        }
        .padding(.vertical)
        .padding(.bottom, 30)
        .background(
            (removeConfirmVisible || enterNameDialogVisible)
            ? Color.black.opacity(0.4)
            : Color.fromInt(0xECECEC)
        )
        .clipShape(RoundedRectangle(cornerRadius: 25))
        .frame(maxHeight: .infinity, alignment: .bottom)
        .transition(.offset(y: 400))
        .ignoresSafeArea()
    }
    
}

struct TemplatesActionDialog_Previews: PreviewProvider {
    static var previews: some View {
        TemplatesActionDialog(templateName: "test") { _,_  in }
    }
}
