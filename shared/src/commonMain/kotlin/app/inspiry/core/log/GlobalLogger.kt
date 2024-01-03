package app.inspiry.core.log

import app.inspiry.core.manager.DebugManager
import com.soywiz.klock.DateTime

object GlobalLogger {

    inline fun verbose(tag: String, isLogEnabled: Boolean = DebugManager.isDebug, message: () -> String) {
        if (isLogEnabled) {
            PlatformLogger.verbose(tag, message())
        }
    }

    inline fun debug(tag: String, isLogEnabled: Boolean = DebugManager.isDebug, message: () -> String) {
        if (isLogEnabled) {
            PlatformLogger.debug(tag, message())
        }
    }

    inline fun info(tag: String, isLogEnabled: Boolean = DebugManager.isDebug, message: () -> String) {
        if (isLogEnabled) {
            PlatformLogger.info(tag, message())
        }
    }

    inline fun warning(tag: String, isLogEnabled: Boolean = DebugManager.isDebug, message: () -> String) {
        if (isLogEnabled) {
            PlatformLogger.warning(tag, message())
        }
    }

    inline fun error(tag: String, isLogEnabled: Boolean = DebugManager.isDebug, t: Throwable? = null, message: () -> String = { "" }) {
        if (isLogEnabled) {
            PlatformLogger.error(tag, t, message())
        }
    }

    inline fun infoTime(tag: String, isLogEnabled: Boolean = DebugManager.isDebug, message: () -> String, action: () -> Unit) {
        if (isLogEnabled) {
            val time = DateTime.nowUnixLong()
            action()
            val spend = DateTime.nowUnixLong() - time

            PlatformLogger.info(tag, message() + ". Took time ${spend}")
        } else {
            action()
        }
    }
}