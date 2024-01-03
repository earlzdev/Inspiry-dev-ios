package app.inspiry.core.notification

import app.inspiry.core.manager.InspRemoteConfig
import app.inspiry.core.manager.LicenseManager
import app.inspiry.core.log.LoggerGetter
import com.russhwolf.settings.Settings
import com.soywiz.klock.DateTime
import com.soywiz.klock.days
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import kotlin.math.floor

class FreeWeeklyTemplatesNotificationManager(
    private val settings: Settings,
    notificationScheduler: NotificationScheduler,
    private val licenseManager: LicenseManager,
    private val scope: CoroutineScope,
    loggerGetter: LoggerGetter,
    val remoteConfig: InspRemoteConfig,
    val freeTemplatesPeriodDays: Double
): NotificationManager(notificationScheduler) {

    private var makeFreeWeeklyTemplatesAfterInactivityDays: Double = remoteConfig.makeFreeWeeklyTemplatesAfterInactivityDays
    private var weeklyFreeTemplatesAvailableForPeriods: Int = remoteConfig.weeklyFreeTemplatesAvailableForPeriods

    private val logger = loggerGetter.getLogger("WeeklyAlarmManager")
    private val _currentWeekIndex = MutableStateFlow(getFreeThisWeekIndex())
    //if null then don't send notification and don't make templates available.
    val currentWeekIndex: StateFlow<Int?> = _currentWeekIndex


    override fun onRemoteConfigActivated(remoteConfig: InspRemoteConfig) {
        makeFreeWeeklyTemplatesAfterInactivityDays = remoteConfig.makeFreeWeeklyTemplatesAfterInactivityDays
        weeklyFreeTemplatesAvailableForPeriods = remoteConfig.weeklyFreeTemplatesAvailableForPeriods

        onPremiumChanged(licenseManager.hasPremiumState.value)
        scope.launch {

            licenseManager.hasPremiumState.drop(1).collect {
                onPremiumChanged(it)
                //this should be called only on change.
                recomputeCurrentWeekIndex()
            }
        }
    }

    private fun onPremiumChanged(it: Boolean) {
        if (it) {
            cancelNotification()
        } else {
            initRepeatNotifications()
        }
    }

    override fun onSendNotification() {

        if (isDisabled()) {
            notificationScheduler.cancelNotification(NotificationType.WEEKLY_FREE_TEMPLATES)
            return
        }

        if (!settings.hasKey(PREF_FREE_FOR_WEEK_STARTED_TIME)) {
            settings.putLong(PREF_FREE_FOR_WEEK_STARTED_TIME, DateTime.nowUnixLong())
        }

        val index = getFreeThisWeekIndex()

        logger.info { "onGetAlarm index ${index}, isPremium ${licenseManager.hasPremiumState.value}, weeklyFreeTemplatesAvailableForPeriods $weeklyFreeTemplatesAvailableForPeriods" }

        if (index == null) notificationScheduler.cancelNotification(NotificationType.WEEKLY_FREE_TEMPLATES)

        recomputeCurrentWeekIndex(index)
    }

    override fun getType(): NotificationType {
        return NotificationType.WEEKLY_FREE_TEMPLATES
    }


    fun debugMoveToNextPeriod(forceEvenIfNoSettings: Boolean) {
        var startedTime = settings.getLong(PREF_FREE_FOR_WEEK_STARTED_TIME)

        val res: Long = if (startedTime == 0L) {
            startedTime = DateTime.nowUnixLong()

            if (forceEvenIfNoSettings) startedTime
            else startedTime - freeTemplatesPeriodDays.days.millisecondsLong
        } else
            startedTime - freeTemplatesPeriodDays.days.millisecondsLong

        settings.putLong(PREF_FREE_FOR_WEEK_STARTED_TIME, res)

        recomputeCurrentWeekIndex()
    }


    private fun isDisabled(): Boolean {
        return (licenseManager.hasPremiumState.value
                && !DEBUG_NOTIFICATION
                ) || makeFreeWeeklyTemplatesAfterInactivityDays <= 0
    }

    private fun initRepeatNotifications() {
        if (settings.hasKey(PREF_FREE_FOR_WEEK_STARTED_TIME))
            return

        notificationScheduler.cancelNotification(NotificationType.WEEKLY_FREE_TEMPLATES)

        logger.info { "initRepeatNotifications ${isDisabled()}, makeFreeWeekly ${makeFreeWeeklyTemplatesAfterInactivityDays}" }
        if (isDisabled()) {
            return
        }

        val enableCategoryTime = (DateTime.now() + makeFreeWeeklyTemplatesAfterInactivityDays.days)

        notificationScheduler.repeatedNotificationAt(
            enableCategoryTime.unixMillisLong,
            freeTemplatesPeriodDays.days.millisecondsLong, NotificationType.WEEKLY_FREE_TEMPLATES
        )
    }

    private inline fun recomputeCurrentWeekIndex(index: Int? = getFreeThisWeekIndex()) {
        _currentWeekIndex.tryEmit(index)
        logger.info { "recomputeCurrentWeekIndex ${index}, " +
                "value in state ${currentWeekIndex.value}, weeklyFreeTemplatesAvailableForPeriods ${weeklyFreeTemplatesAvailableForPeriods}" }
    }

    //index starts from 0
    private fun getFreeThisWeekIndex(): Int? {
        if (isDisabled()) {
            return null
        }

        val freeForWeekStarted = settings.getLong(PREF_FREE_FOR_WEEK_STARTED_TIME)
        if (freeForWeekStarted == 0L) {
            return null
        }

        val now = DateTime.nowUnixLong()

        val difference = (now - freeForWeekStarted) / freeTemplatesPeriodDays.days.milliseconds

        val index = floor(difference).toInt()

        if (index >= weeklyFreeTemplatesAvailableForPeriods)
            return null

        return index
    }


    companion object {
        private val DEBUG_NOTIFICATION = false
        private const val PREF_FREE_FOR_WEEK_STARTED_TIME = "free_for_week_started"
        const val START_INSTANTLY_TIME = 0.0000001
    }
}