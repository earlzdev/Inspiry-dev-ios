package app.inspiry.core.notification

import app.inspiry.core.manager.InspRemoteConfig
import app.inspiry.core.log.KLogger
import app.inspiry.core.log.LoggerGetter
import com.russhwolf.settings.Settings
import com.soywiz.klock.DateTime
import com.soywiz.klock.hours
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class StoryUnfinishedNotificationManager(
    private val settings: Settings,
    private val config: InspRemoteConfig,
    notificationScheduler: NotificationScheduler,
    private val json: Json,
    loggerGetter: LoggerGetter, private val isDebug: Boolean
) : NotificationManager(notificationScheduler) {

    private val logger: KLogger = loggerGetter.getLogger("NotificationManager")

    fun onStorySaved(storyPath: String) {
        logger.info {
            "onStorySaved ${storyPath}, isActive ${isActive()} " +
                    "isScheduled ${notificationScheduler.isNotificationScheduled(NotificationType.UNFINISHED_STORY)}"
        }

        if (!isActive())
            return

        addUnfinishedStory(storyPath)

        logger.info {
            "after onStorySaved ${getUnfinishedStories()}"
        }

        if (!notificationScheduler.isNotificationScheduled(NotificationType.UNFINISHED_STORY)) {

            val time =
                DateTime.nowUnixLong() + config.notifyStoryIsUnfinishedAfterHours.hours.millisecondsLong

            notificationScheduler.oneTimeNotificationAt(time, NotificationType.UNFINISHED_STORY)
        }
    }

    fun onStoryRendered(storyPath: String) {
        if (!isActive())
            return

        removeUnfinishedStory(storyPath)

        if (getUnfinishedStories().isEmpty())
            notificationScheduler.cancelNotification(NotificationType.UNFINISHED_STORY)
    }

    fun getUnfinishedStoryPathWhenNotificationSend(): String? {

        val unfinishedStories = getUnfinishedStories()
        val result = unfinishedStories.lastOrNull()

        removeUnfinishedStories()
        settings.putLong(SEND_NOTIFICATION_LAST_TIME_KEY, DateTime.nowUnixLong())

        if (isDebug && result == null) throw IllegalStateException("unfinished story path is null")

        return result
    }

    private fun isActive(): Boolean {
        val sendAfter = config.notifyStoryIsUnfinishedAfterHours
        if (sendAfter <= 0L) return false

        val sendInterval = config.notificationAllowedIntervalHours
        val lastTimeSend = settings.getLong(SEND_NOTIFICATION_LAST_TIME_KEY, 0)

        if (DateTime.nowUnixLong() - lastTimeSend < sendInterval.hours.millisecondsLong)
            return false

        return true
    }

    private fun getUnfinishedStories(): List<String> {

        val settingsRes = settings.getString(KEY_UNFINISHED_STORIES, defaultValue = "")
        if (settingsRes.isBlank())
            return emptyList()

        return json.decodeFromString(settingsRes)
    }

    private fun saveUnfinishedStories(list: List<String>) {
        val string = json.encodeToString(list)
        settings.putString(KEY_UNFINISHED_STORIES, string)
    }

    private fun addUnfinishedStory(storyPath: String) {
        val stories = getUnfinishedStories().toMutableList()
        stories.add(storyPath)

        saveUnfinishedStories(stories)
    }

    private fun removeUnfinishedStory(storyPath: String) {
        val stories = getUnfinishedStories().toMutableList()
        stories.remove(storyPath)

        saveUnfinishedStories(stories)
    }

    private fun removeUnfinishedStories() {
        settings.remove(KEY_UNFINISHED_STORIES)
    }

    companion object {
        private const val KEY_UNFINISHED_STORIES = "unfinished_stories"
        private const val SEND_NOTIFICATION_LAST_TIME_KEY = "send_last_time_unfinished_story"
    }

    override fun onRemoteConfigActivated(remoteConfig: InspRemoteConfig) {
        // nothing
    }

    override fun onSendNotification() {
        // nothing
    }

    override fun getType(): NotificationType = NotificationType.UNFINISHED_STORY
}