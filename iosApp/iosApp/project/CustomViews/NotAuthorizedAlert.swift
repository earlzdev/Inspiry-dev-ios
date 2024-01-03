//
//  NotAuthorizedAlert.swift
//  iosApp
//
//  Created by rst10h on 6.01.23.
//

import SwiftUI

struct NotAuthorizedAlert: Alert {
    var body: some Alert {
        Alert(title: Text("Нет доступа к медиатеке"), message: Text("Откройте доступ к медиатеке, чтобы у вас была возможность добавить ваши фото в шаблон или сохранить ваши сториз"), primaryButton: .default(Text("Settings") {print ("settings pressed")}), secondaryButton: .cancel(Text("Cancel")) {print("cancel pressed")})
    }
}

struct NotAuthorizedAlert_Previews: PreviewProvider {
    static var previews: some View {
        NotAuthorizedAlert()
    }
}
