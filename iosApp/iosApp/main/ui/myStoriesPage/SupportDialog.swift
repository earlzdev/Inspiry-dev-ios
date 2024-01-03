//
//  SupportDialog.swift
//  iosApp
//
//  Created by rst10h on 31.12.21.
//

import SwiftUI
import shared

struct SupportDialog: View {
    private let remoteConfig = Dependencies.diContainer.resolve(InspRemoteConfig.self)!
    @Binding var isVisible: Bool
    private let colors = MainScreenColorsLight()
    private let version = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String
    var body: some View {
        ZStack(alignment: .bottom) {
            VStack(alignment: .leading) {
                Text(MR.strings().feedback_title.localized())
                    .foregroundColor(.black)
                    .font(.system(size: 20))
                    .fontWeight(.bold)
                    .padding(.bottom, 15)
                let feedbackMessage = MR.strings().feedback_message.localized()
                    .replacingOccurrences(of: "%s", with: version ?? "unknown")
                Text(feedbackMessage)
                    .foregroundColor(.black)
                    .font(.system(size: 16))
                Text(remoteConfig.getString(key: "support_email"))
                    .foregroundColor(.black)
                    .font(.system(size: 16))
                    .padding(.bottom, 10)
                HStack(spacing: 0) {
                    Spacer()
                    Button(action: {
                        withAnimation { isVisible.toggle() }}, label: {
                            Text(MR.strings().cancel.localized())
                                .foregroundColor(colors.instagramLinkTextColor.toSColor())
                                .font(.system(size: 16))
                                .fontWeight(.bold)
                                .textCase(.uppercase)
                                .padding(.trailing, 20)
                        }
                    )
                    Button(action: {
                        DispatchQueue.main.async {
                            openMail(emailTo: remoteConfig.getString(key: "support_email"), subject: "Inspiry version \(version ?? "unknown") support", body: "some text")
                        }
                    }, label: {
                        Text(MR.strings().feedback_positive_button.localized())
                            .foregroundColor(colors.instagramLinkTextColor.toSColor())
                            .font(.system(size: 16))
                            .fontWeight(.bold)
                            .textCase(.uppercase)
                    })
                    
                }
            }
            .padding(.horizontal, 30)
            .padding(.bottom, 50)
            .padding(.top, 25)
            .background(Color.white)
            .clipShape(RoundedRectangle(cornerRadius: 15))
        }
        .frame(maxHeight: .infinity, alignment: .bottom)
        .transition(.offset(y: 400))
        .ignoresSafeArea()
    }
    
    func openMail(emailTo:String, subject: String, body: String) {
        if let url = URL(string: "mailto:\(emailTo)?subject=\(subject.fixToBrowserString())&body=\(body.fixToBrowserString())"),
           UIApplication.shared.canOpenURL(url)
        {
            UIApplication.shared.open(url, options: [:], completionHandler: nil)
        }
    }

}

extension String {
    func fixToBrowserString() -> String {
        self.replacingOccurrences(of: ";", with: "%3B")
            .replacingOccurrences(of: "\n", with: "%0D%0A")
            .replacingOccurrences(of: " ", with: "%20")
            .replacingOccurrences(of: "!", with: "%21")
            .replacingOccurrences(of: "\"", with: "%22")
            .replacingOccurrences(of: "\\", with: "%5C")
            .replacingOccurrences(of: "/", with: "%2F")
            .replacingOccurrences(of: "‘", with: "%91")
            .replacingOccurrences(of: ",", with: "%2C")
    }
}

struct SupportDialog_Previews: PreviewProvider {
    @State static var isVisible = true
    static var previews: some View {
        SupportDialog(isVisible: $isVisible)
            .preferredColorScheme(.dark)
    }
}
