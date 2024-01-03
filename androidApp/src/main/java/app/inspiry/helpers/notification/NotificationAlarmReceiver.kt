package app.inspiry.helpers.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import app.inspiry.activities.MainActivity
import app.inspiry.core.log.KLogger
import app.inspiry.core.notification.FreeWeeklyTemplatesNotificationManager
import app.inspiry.core.notification.NotificationType
import app.inspiry.core.notification.StoryUnfinishedNotificationManager
import app.inspiry.utils.Constants
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.parameter.parametersOf

class NotificationAlarmReceiver : BroadcastReceiver(), KoinComponent {

    private val notificationSender: NotificationSenderAndroid by inject()
    private val logger: KLogger by inject {
        parametersOf("NotificationManager")
    }

    override fun onReceive(context: Context, intent: Intent) {
        logger.info {
            "onReceiveAlarm action ${intent.action}"
        }

        val action = intent.action ?: return

        if (action == Constants.ACTION_FROM_NOTIFICATION) {

            val type = NotificationType.valueOf(intent.getStringExtra(Constants.EXTRA_NOTIFICATION_TYPE)!!)
            val bundle = intent.getBundleExtra(Constants.EXTRA_NAVIGATION_BUNDLE)!!

            notificationSender.onTriggerNotification(type, bundle)
        }
    }
}
