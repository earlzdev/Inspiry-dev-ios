//
//  NotificationShedulerApple.swift
//  iosApp
//
//  Created by rst10h on 29.01.22.
//

import Foundation
import shared

class NotificationSchedulerApple: NotificationScheduler {
    func cancelNotification(notificationType: NotificationType) {
        //todo
    }
    
    func isNotificationScheduled(notificationType: NotificationType) -> Bool {
        return false
    }
    
    func oneTimeNotificationAt(triggerAt: Int64, notificationType: NotificationType, navigationData: [String : Any]) {
        //todo
    }
    
    func repeatedNotificationAt(triggerAt: Int64, interval: Int64, notificationType: NotificationType, navigationData: [String : Any]) {
        //todo
    }
}
