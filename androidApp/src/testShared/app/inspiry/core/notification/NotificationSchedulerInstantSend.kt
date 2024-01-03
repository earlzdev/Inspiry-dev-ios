package app.inspiry.core.notification

class NotificationSchedulerInstantSend : NotificationScheduler {
    lateinit var callback: (notificationType: NotificationType,
                            navigationData: Map<String, Any?>) -> Unit

    var isScheduled = false

    override fun oneTimeNotificationAt(
        triggerAt: Long,
        notificationType: NotificationType,
        navigationData: Map<String, Any?>
    ) {
        callback(notificationType, navigationData)
    }

    override fun repeatedNotificationAt(
        triggerAt: Long,
        interval: Long,
        notificationType: NotificationType,
        navigationData: Map<String, Any?>
    ) {
        isScheduled = true
        val max = 100
        var i = 0
        while (isScheduled) {
            callback(notificationType, navigationData)
            i++
            if (i >= max) throw IllegalStateException("endless loop")
        }
        isScheduled = false
    }

    override fun cancelNotification(notificationType: NotificationType) {
        isScheduled = false
    }

    override fun isNotificationScheduled(notificationType: NotificationType): Boolean {
        return isScheduled
    }
}