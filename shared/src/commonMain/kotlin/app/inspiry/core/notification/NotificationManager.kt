package app.inspiry.core.notification

import app.inspiry.core.manager.InspRemoteConfig

abstract class NotificationManager(protected val notificationScheduler: NotificationScheduler) {
    abstract fun onRemoteConfigActivated(remoteConfig: InspRemoteConfig)
    abstract fun onSendNotification()
    abstract fun getType(): NotificationType

    fun cancelNotification() {
        notificationScheduler.cancelNotification(getType())
    }
}