package app.inspiry.core.notification

import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.analytics.putInt
import app.inspiry.core.analytics.putString

abstract class NotificationSender<NavigationData>(
    protected val analyticsManager: AnalyticsManager,
    protected val notificationManagersContainer: NotificationManagersContainer
) {

    fun onTriggerNotification(type: NotificationType, navigationData: NavigationData) {

        var weekNumber: Int? = null

        val notificationManagers = notificationManagersContainer.list

        notificationManagers.forEach { it.onSendNotification() }

        when (type) {
            NotificationType.UNFINISHED_STORY -> {
                val storyUnfinishedNotificationManager =
                    notificationManagers.find { it.getType() == NotificationType.UNFINISHED_STORY } as StoryUnfinishedNotificationManager
                val path =
                    storyUnfinishedNotificationManager.getUnfinishedStoryPathWhenNotificationSend()
                        ?: return
                addOpenMyStoryPath(path, navigationData)
            }
            NotificationType.WEEKLY_FREE_TEMPLATES -> {
                val weeklyTemplatesNotificationManager =
                    notificationManagers.find { it.getType() == NotificationType.WEEKLY_FREE_TEMPLATES } as FreeWeeklyTemplatesNotificationManager
                weeklyTemplatesNotificationManager.onSendNotification()
                weekNumber = weeklyTemplatesNotificationManager.currentWeekIndex.value
            }
            else -> {}
        }

        analyticsManager.sendEvent("notification_send") {
            putString("id", type.toString())
            if (weekNumber != null) {
                putInt(
                    "week_number",
                    weekNumber
                )
            }
        }
        onTriggerNotificationInner(type, navigationData)
    }


    protected abstract fun addOpenMyStoryPath(path: String, navigationData: NavigationData)

    protected abstract fun onTriggerNotificationInner(
        type: NotificationType,
        navigationData: NavigationData
    )
}