package app.inspiry.core.manager

import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

abstract class InspRemoteConfig {
    abstract fun getBoolean(key: String): Boolean
    abstract fun getString(key: String): String
    abstract fun getDouble(key: String): Double
    abstract fun getLong(key: String): Long

    // null if activated
    private var isActivatedThisSession: Job? = Job()

    private var onActivated: (() -> Unit)? = null

    fun doWhenValuesActivated(action: () -> Unit) {
        if (isActivatedThisSession != null)
            action()
        else
            onActivated = action
    }

    suspend fun getValueWhenActivatedWithTimeout(key: String): String  {
        val isActivatedThisSession = isActivatedThisSession
        return if (isActivatedThisSession == null) {
            getString(key)
        } else {
            try {
                withTimeout(15000) {
                    isActivatedThisSession.join()
                    getString(key)
                }
            } catch (e: TimeoutCancellationException) {
                getString(key)
            }
        }
    }

    protected fun onActivated() {
        isActivatedThisSession?.cancel()
        isActivatedThisSession = null

        if (onActivated != null) {
            onActivated?.invoke()
            onActivated = null
        }
    }

    fun getInt(key: String) = getLong(key).toInt()
    fun getFloat(key: String) = getDouble(key).toFloat()

    //seconds
    val minimumFetchInterval: Int
        get() = 7200

    open val makeFreeWeeklyTemplatesAfterInactivityDays: Double
        get() = getDouble("make_free_weekly_templates_after_inactivity_days")

    open val weeklyFreeTemplatesAvailableForPeriods: Int
        get() = getInt("weekly_free_templates_available_for_periods")

    open val templatesAvailabilityTest: Int
        get() = getInt("templates_availability_test")

    open val removeBgNotifyAfterDays: Double
        get() = if (DebugManager.isDebug) -1.0 else getDouble("remove_bg_notify_after_days")

    open val discountNotificationAfterDays: Double
        get() = if (DebugManager.isDebug) -1.0 else getDouble("notify_discount_after_days")

    open val notifyStoryIsUnfinishedAfterHours: Double
        get() = getDouble("notify_story_is_unfinished_after_hours")

    open val notificationAllowedIntervalHours: Long
        get() = getLong("notify_story_is_unfinished_allowed_interval_hours")
}