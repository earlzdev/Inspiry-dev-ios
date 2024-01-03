package app.inspiry.helpers.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import androidx.core.app.NotificationCompat
import app.inspiry.R
import app.inspiry.activities.MainActivity
import app.inspiry.bfpromo.ui.BFPromoActivity
import app.inspiry.core.analytics.AnalyticsManager
import app.inspiry.core.data.UserSavedTemplatePath
import app.inspiry.core.notification.*
import app.inspiry.core.notification.NotificationType.*
import app.inspiry.edit.EditActivity
import app.inspiry.featurepromo.RemoveBgPromoActivity
import app.inspiry.utils.Constants
import app.inspiry.utils.putTemplatePath
import dev.icerock.moko.resources.desc.desc

class NotificationSenderAndroid(
    val context: Context,
    private val notificationProvider: NotificationProvider,
    analyticsManager: AnalyticsManager,
    notificationManagersContainer: NotificationManagersContainer,
) : NotificationSender<Bundle>(
    analyticsManager, notificationManagersContainer
) {

    private fun buildActionFor(notificationType: NotificationType, navigationData: Bundle): Intent {
        val intent = Intent()
        intent.action = Constants.ACTION_FROM_NOTIFICATION
        intent.putExtra(Constants.EXTRA_NOTIFICATION_TYPE, notificationType.toString())


        when (notificationType) {
            UNFINISHED_STORY -> {
                intent.putTemplatePath(
                    UserSavedTemplatePath(
                        navigationData.getString(
                            EXTRA_FILE_PATH
                        )!!
                    )
                )
                intent.setClass(context, EditActivity::class.java)
            }
            WEEKLY_FREE_TEMPLATES -> {
                intent.setClass(context, MainActivity::class.java)
            }
            REMOVE_BG -> {
                intent.setClass(context, RemoveBgPromoActivity::class.java)
            }
            DISCOUNT -> intent.setClass(context, BFPromoActivity::class.java)
                .putExtra(Constants.EXTRA_SOURCE, Constants.ACTION_FROM_NOTIFICATION)
        }

        return intent
    }

    override fun onTriggerNotificationInner(type: NotificationType, navigationData: Bundle) {
        val notification = notificationProvider.provide(type)

        val largeIconId = notification.image?.drawableResId
        val largeIcon: Bitmap? = if (largeIconId == null) null
        else BitmapFactory.decodeResource(context.resources, largeIconId)

        val title = notification.title.desc().toString(context)
        val notificationBuilder =
            NotificationCompat.Builder(context, notification.notificationChannelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(notification.messageBody.desc().toString(context))
                .setContentIntent(
                    PendingIntent
                        .getActivity(
                            context,
                            notification.hashCode(),
                            buildActionFor(notification.type, navigationData),
                            PendingIntent.FLAG_IMMUTABLE
                        )
                )
                .setLargeIcon(largeIcon)
                .setSilent(!notification.sendWithSound)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setColor(0xff0089D6.toInt())
                .setStyle(NotificationCompat.BigTextStyle())

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    notification.notificationChannelId,
                    title, NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }

        notificationManager.notify(notification.hashCode(), notificationBuilder.build())
    }

    override fun addOpenMyStoryPath(path: String, navigationData: Bundle) {
        navigationData.putString(EXTRA_FILE_PATH, path)
    }

    companion object {
        const val EXTRA_FILE_PATH = "file_path"
        fun checkNotificationOpened(intent: Intent, analyticsManager: AnalyticsManager): Boolean {
            if (intent.action == Constants.ACTION_FROM_NOTIFICATION) {
                val type =
                    valueOf(intent.getStringExtra(Constants.EXTRA_NOTIFICATION_TYPE)!!)
                analyticsManager.onNotificationOpened(type.toString())
                return true
            }
            return false
        }
    }
}