//
//  ErrorHandler.swift
//  iosApp
//
//  Created by vlad on 11/11/21.
//

import Foundation
import shared
import Firebase

class ErrorHandlerImpl: ErrorHandler {
    
    override func recordFirebaseException(t: KotlinThrowable) {
        Crashlytics.crashlytics().record(error: t.asError())
    }
}
