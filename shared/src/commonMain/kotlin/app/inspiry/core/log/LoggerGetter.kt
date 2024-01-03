package app.inspiry.core.log

import app.inspiry.core.manager.DebugManager

open class LoggerGetter {
    open fun getLogger(tag: String) = KLogger(DebugManager.isDebug, tag)
}