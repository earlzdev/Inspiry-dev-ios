package app.inspiry.helpers.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import app.inspiry.activities.MainActivity
import app.inspiry.core.notification.NotificationScheduler
import app.inspiry.core.notification.NotificationType
import app.inspiry.helpers.BootCompletedReceiver
import app.inspiry.helpers.analytics.FacebookAnalyticsManagerAndroid
import app.inspiry.utils.Constants

class NotificationSchedulerAndroid(val context: Context): NotificationScheduler {

    private inline fun getAlarmPendingIntent(
        type: NotificationType,
        flag: Int = PendingIntent.FLAG_UPDATE_CURRENT,
        populateIntent: (Intent) -> Unit = {}
    ): PendingIntent? {
        val alarmIntent =
            Intent(context, NotificationAlarmReceiver::class.java)
                .setAction(Constants.ACTION_FROM_NOTIFICATION)
                .putExtra(Constants.EXTRA_NOTIFICATION_TYPE, type.toString())
        populateIntent(alarmIntent)
        return PendingIntent.getBroadcast(context, type.hashCode(), alarmIntent, flag or PendingIntent.FLAG_IMMUTABLE)
    }

    override fun oneTimeNotificationAt(triggerAt: Long, notificationType: NotificationType, navigationData: Map<String, Any?>) {
        toggleBootCompletedReceiver(context, true)

        val manager: AlarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.set(AlarmManager.RTC, triggerAt, getAlarmPendingIntent(notificationType,
            populateIntent = getPopulateIntentFromNavigationData(navigationData)))
    }

    private fun getPopulateIntentFromNavigationData(navigationData: Map<String, Any?>): (Intent) -> Unit {
        return { intent ->
            val bundle = FacebookAnalyticsManagerAndroid.mapToBundle(navigationData)
            intent.putExtra(Constants.EXTRA_NAVIGATION_BUNDLE, bundle)
        }
    }

    private fun toggleBootCompletedReceiver(context: Context, enable: Boolean) {
        val receiver = ComponentName(context, BootCompletedReceiver::class.java)

        context.packageManager.setComponentEnabledSetting(
            receiver,
            if (enable)
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    override fun repeatedNotificationAt(
        triggerAt: Long,
        interval: Long,
        notificationType: NotificationType, navigationData: Map<String, Any?>
    ) {
        toggleBootCompletedReceiver(context, true)

        val manager: AlarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        manager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            interval,
            getAlarmPendingIntent(notificationType,
                populateIntent = getPopulateIntentFromNavigationData(navigationData))
        )
    }

    override fun cancelNotification(notificationType: NotificationType) {
        val manager: AlarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = getAlarmPendingIntent(notificationType, PendingIntent.FLAG_NO_CREATE)
        if (intent != null) {
            manager.cancel(intent)
            toggleBootCompletedReceiver(context, false)
        }
    }

    override fun isNotificationScheduled(notificationType: NotificationType): Boolean {
        return getAlarmPendingIntent(notificationType, PendingIntent.FLAG_NO_CREATE) != null
    }
}