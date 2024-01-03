package app.inspiry.core.notification

import app.inspiry.core.analytics.AnalyticsManager

class NotificationSenderCount(
    analyticsManager: AnalyticsManager,
    notificationManagersContainer: NotificationManagersContainer,

    ) : NotificationSender<Map<String, Any?>>(
    analyticsManager, notificationManagersContainer,
) {

    var sendCount = 0

    override fun addOpenMyStoryPath(path: String, navigationData: Map<String, Any?>) {

    }

    override fun onTriggerNotificationInner(
        type: NotificationType,
        navigationData: Map<String, Any?>
    ) {
        sendCount += 1
    }
}