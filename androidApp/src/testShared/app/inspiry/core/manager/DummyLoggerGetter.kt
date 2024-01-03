package app.inspiry.core.manager

import app.inspiry.core.log.KLogger
import app.inspiry.core.log.LoggerGetter

class DummyLoggerGetter: LoggerGetter() {

    override fun getLogger(tag: String): KLogger {
        return DummyKLogger
    }
}