package app.inspiry.core.log

open class KLogger(var isLogEnabled: Boolean, var tag: String) {

    inline fun verbose(message: () -> String) {
        GlobalLogger.verbose(tag, isLogEnabled, message)
    }

    inline fun debug(message: () -> String) {
        GlobalLogger.debug(tag, isLogEnabled, message)
    }

    inline fun info(message: () -> String) {
        GlobalLogger.info(tag, isLogEnabled, message)
    }

    inline fun warning(message: () -> String) {
        GlobalLogger.warning(tag, isLogEnabled, message)
    }

    inline fun error(t: Throwable? = null, message: () -> String = { "" }) {
        GlobalLogger.error(tag, isLogEnabled, t, message)
    }
}