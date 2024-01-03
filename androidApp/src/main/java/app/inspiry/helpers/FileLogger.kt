package app.inspiry.helpers

import android.annotation.SuppressLint
import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * @param splitPeriodMin interval for writing data to one file
 */
class FileLogger(
        context: Context,
        private val splitPeriodMin: Int
) {

    private val logDir = File(context.externalCacheDir, FILE_NAME)

    init {
        removePreviousDateLogFiles()
    }

    private fun removePreviousDateLogFiles() {
        if (logDir.exists()) {
            val currentDate = GregorianCalendar.getInstance().date()
            logDir.listFiles { _, name -> !name.startsWith(currentDate) }
                    ?.forEach { it.delete() }
        }
    }

    private fun Calendar.date() = String.format(DATE_PATTERN, get(Calendar.DAY_OF_MONTH).withZero(),
            (get(Calendar.MONTH) + 1).withZero())

    private fun Int.withZero() = if (this <= 9) "0$this" else "$this"

    fun log(msg: String) {
        val logFile = File(logDir, fileName())
        if (!logFile.exists()) {
            logDir.mkdirs()
            logFile.createNewFile()
        }
        logFile.appendText("${logTime()}:$msg\n")
    }

    private fun fileName(): String {
        val calendar = GregorianCalendar.getInstance()
        return String.format(FILE_NAME_PATTERN, calendar.date(), calendar.time()) + FILE_EXTENSION
    }

    private fun Calendar.time(): String {
        val minuteCount = get(Calendar.HOUR_OF_DAY) * 60 + get(Calendar.MINUTE)
        val roundMinute = (minuteCount / splitPeriodMin) * splitPeriodMin
        val h = roundMinute / 60
        val m = roundMinute % 60
        return String.format(TIME_PATTERN, h.withZero(), m.withZero())
    }

    @SuppressLint("SimpleDateFormat")
    private fun logTime() =
            SimpleDateFormat(TIME_STAMP_PATTERN).format(System.currentTimeMillis())

    fun log(throwable: Throwable?, msg: String) {
        val error = StringBuilder(msg)
                .appendMessage(throwable)
                .appendStackTrace(throwable)
                .toString()
        log(error)
    }

    private fun StringBuilder.appendMessage(ex: Throwable?) = apply {
        val message = ex?.message ?: return this
        append(", message = $message")
    }

    private fun StringBuilder.appendStackTrace(ex: Throwable?) = apply {
        val stackTraceArray = ex?.stackTrace ?: return this
        append("\n")
        for (stackTrace in stackTraceArray) {
            append(stackTrace).append("\n")
        }
    }

    companion object {
        private const val TIME_PATTERN = "%s.%s"
        private const val DATE_PATTERN = "%s.%s"
        private const val FILE_NAME_PATTERN = "%s_%s" // date_time
        private const val TIME_STAMP_PATTERN = "HH:mm:ss.SSS"
        private const val FILE_EXTENSION = ".txt"
        private const val FILE_NAME = "logs"
    }
}