package app.inspiry.core.notification

import app.inspiry.MR

class NotificationProvider {

    private val weeklyFreeTemplatesNotification = NotificationData(
        NotificationType.WEEKLY_FREE_TEMPLATES,
        MR.strings.weekly_notification_title,
        MR.strings.weekly_notification_subtitle,
        "weekly_update",
        image = MR.images.notif_large_icon_free_weekly_templates,
        sendWithSound = false
    )

    private val unfinishedStoryNotification = NotificationData(
        NotificationType.UNFINISHED_STORY,
        MR.strings.notification_unfinished_story_title,
        MR.strings.notification_unfinished_story_subtitle,
        "unfinished_story",
        image = MR.images.notif_large_icon_unfinished_story,
        sendWithSound = false
    )

    private val removeBgNotification = NotificationData(
        NotificationType.REMOVE_BG,
        MR.strings.remove_bg_notification_title,
        MR.strings.remove_bg_notification_subtitle,
        "promo", image = MR.images.notification_remove_bg, sendWithSound = false
    )

    private val notificationDiscount = NotificationData(
        NotificationType.DISCOUNT,
        MR.strings.bf_special_offer,
        MR.strings.bf_notification_subtitle,
        "promo", image = MR.images.notification_discount, sendWithSound = false

    )

    private val notifications: List<NotificationData> =
        listOf(weeklyFreeTemplatesNotification, unfinishedStoryNotification, removeBgNotification, notificationDiscount)

    fun provide(type: NotificationType): NotificationData {
        return notifications.find { it.type == type }
            ?: throw IllegalStateException("notification not found error $type")
    }
}