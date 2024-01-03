package app.inspiry.core.notification

interface NotificationScheduler {
    fun oneTimeNotificationAt(triggerAt: Long, notificationType: NotificationType,
                              navigationData: Map<String, Any?> = emptyMap())
    fun repeatedNotificationAt(triggerAt: Long, interval: Long,
                               notificationType: NotificationType, navigationData: Map<String, Any?> = emptyMap())

    fun cancelNotification(notificationType: NotificationType)
    fun isNotificationScheduled(notificationType: NotificationType): Boolean
}