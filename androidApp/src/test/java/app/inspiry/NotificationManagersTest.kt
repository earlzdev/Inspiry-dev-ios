package app.inspiry

import app.inspiry.core.analytics.EmptyAnalyticsManager
import app.inspiry.core.analytics.EmptyRemoteConfig
import app.inspiry.core.manager.DummyLoggerGetter
import app.inspiry.core.manager.LicenseManagerNotPremium
import app.inspiry.core.notification.*
import com.russhwolf.settings.MockSettings
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

open class NotificationManagersTest {

    val settings: Settings = MockSettings()
    val remoteConfig = object : EmptyRemoteConfig() {

        override val makeFreeWeeklyTemplatesAfterInactivityDays: Double
            get() = FreeWeeklyTemplatesNotificationManager.START_INSTANTLY_TIME
        override val weeklyFreeTemplatesAvailableForPeriods: Int
            get() = weeklyFreeTemplatesAvilableForPeriods

        override val notifyStoryIsUnfinishedAfterHours: Double
            get() = 0.001
        override val notificationAllowedIntervalHours: Long
            get() = 0

        override val removeBgNotifyAfterDays: Double
            get() = -1.0 //instantly.
    }
    val licenseManager = LicenseManagerNotPremium(
        settings,
        remoteConfig,
        alwaysFreeVersion = false,
        EmptyAnalyticsManager()
    )
    val loggerGetter = DummyLoggerGetter()
    val scheduler = NotificationSchedulerInstantSend()

    val scope = TestScope()


    val weeklyFreeTemplatesAvilableForPeriods = 4
    val freeTemplatesPeriodDays = 7.0

    /**
    Test UnfinishedStoryNotifManager
     */

    fun getWeeklyTemplatesNotifManager() =
        FreeWeeklyTemplatesNotificationManager(
            settings,
            scheduler,
            licenseManager, scope,
            loggerGetter,
            remoteConfig,
            freeTemplatesPeriodDays = freeTemplatesPeriodDays
        )


    @Test
    fun simplyTestOnStorySavedInstantResponse() {

        val removeBgNotificationManager =
            RemoveBgNotificationManager(settings, scheduler, licenseManager, scope)
        val storyUnfinishedNotificationManager =
            StoryUnfinishedNotificationManager(
                settings,
                remoteConfig, scheduler, Json.Default, loggerGetter, isDebug = true
            )
        val notificationSenderCount = NotificationSenderCount(
            EmptyAnalyticsManager(),
            NotificationManagersContainer(
                listOf(
                    storyUnfinishedNotificationManager,
                    getWeeklyTemplatesNotifManager(),
                    removeBgNotificationManager
                )
            )
        )

        scheduler.callback = notificationSenderCount::onTriggerNotification

        for (i in 1..5) {
            storyUnfinishedNotificationManager.onStorySaved(i.toString())
            assertEquals(i, notificationSenderCount.sendCount)
        }
    }

    @Test
    fun testWithoutSendFirst() {

        val removeBgNotificationManager =
            RemoveBgNotificationManager(settings, scheduler, licenseManager, scope)
        val storyUnfinishedNotificationManager =
            StoryUnfinishedNotificationManager(
                settings,
                remoteConfig, scheduler, Json.Default, loggerGetter, isDebug = true
            )
        val notificationSenderCount = NotificationSenderCount(
            EmptyAnalyticsManager(),
            NotificationManagersContainer(
                listOf(
                    storyUnfinishedNotificationManager,
                    getWeeklyTemplatesNotifManager(),
                    removeBgNotificationManager
                )
            )
        )

        scheduler.callback = { type, data ->
        }

        for (i in 1..5) {
            storyUnfinishedNotificationManager.onStorySaved(i.toString())
            assertEquals(0, notificationSenderCount.sendCount)
        }


        scheduler.callback = notificationSenderCount::onTriggerNotification
        for (i in 1..5) {
            storyUnfinishedNotificationManager.onStorySaved(i.toString())
            assertEquals(i, notificationSenderCount.sendCount)
        }
    }


    @Test
    fun testSendInterval() {

        val config = object : EmptyRemoteConfig() {

            override val notifyStoryIsUnfinishedAfterHours: Double
                get() = 0.1
            override val notificationAllowedIntervalHours: Long
                get() = 10000
        }

        val removeBgNotificationManager =
            RemoveBgNotificationManager(settings, scheduler, licenseManager, scope)
        val storyUnfinishedNotificationManager =
            StoryUnfinishedNotificationManager(
                settings, config, scheduler, Json.Default, loggerGetter, isDebug = true
            )
        val notificationSenderCount = NotificationSenderCount(
            EmptyAnalyticsManager(),
            NotificationManagersContainer(
                listOf(
                    storyUnfinishedNotificationManager,
                    getWeeklyTemplatesNotifManager(),
                    removeBgNotificationManager
                )
            )
        )

        scheduler.callback = notificationSenderCount::onTriggerNotification

        for (i in 1..5) {
            storyUnfinishedNotificationManager.onStorySaved(i.toString())
            assertEquals(1, notificationSenderCount.sendCount)
        }
    }

    /**
    Test FreeWeeklyTemplatesNotificationManager
     */

    @Test
    fun testFreeForWeekTemplates() {

        val removeBgNotificationManager =
            RemoveBgNotificationManager(settings, scheduler, licenseManager, scope)
        val storyUnfinishedNotificationManager =
            StoryUnfinishedNotificationManager(
                settings,
                remoteConfig, scheduler, Json.Default, loggerGetter, isDebug = true
            )

        val manager = getWeeklyTemplatesNotifManager()
        val notificationSenderCount = NotificationSenderCount(
            EmptyAnalyticsManager(),
            NotificationManagersContainer(
                listOf(
                    storyUnfinishedNotificationManager,
                    manager,
                    removeBgNotificationManager
                )
            )
        )

        scheduler.callback = { type, data ->

            manager.debugMoveToNextPeriod(forceEvenIfNoSettings = false)
            notificationSenderCount.onTriggerNotification(type, data)
        }

        manager.onRemoteConfigActivated(remoteConfig)

        assertEquals(weeklyFreeTemplatesAvilableForPeriods, notificationSenderCount.sendCount)
    }

    @Test
    fun testRemoveBgNotificationManager() {

        val removeBgNotificationManager =
            RemoveBgNotificationManager(settings, scheduler, licenseManager, scope)
        val storyUnfinishedNotificationManager =
            StoryUnfinishedNotificationManager(
                settings,
                remoteConfig, scheduler, Json.Default, loggerGetter, isDebug = true
            )

        val manager = getWeeklyTemplatesNotifManager()

        val notificationSenderCount = NotificationSenderCount(
            EmptyAnalyticsManager(),
            NotificationManagersContainer(
                listOf(
                    storyUnfinishedNotificationManager,
                    manager,
                    removeBgNotificationManager
                )
            )
        )

        scheduler.callback = notificationSenderCount::onTriggerNotification

        // should send notification only once.
        for (i in 1..5) {
            removeBgNotificationManager.onRemoteConfigActivated(remoteConfig)
            assertEquals(1, notificationSenderCount.sendCount)
        }
    }
}