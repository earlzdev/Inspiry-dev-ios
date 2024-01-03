package app.inspiry.core.log

actual object PlatformLogger {

    private fun buildMessage(tag: String, message: String, logLevel: String): String {
        return "$logLevel $tag - $message"
    }

    actual fun verbose(tag: String, message: String) {
        println(buildMessage(tag, message, VERBOSE_TXT))
    }

    actual fun debug(tag: String, message: String) {
        println(buildMessage(tag, message, DEBUG_TXT))
    }

    actual fun info(tag: String, message: String) {
        println(buildMessage(tag, message, INFO_TXT))
    }

    actual fun warning(tag: String, message: String) {
        println(buildMessage(tag, message, WARNING_TXT))
    }

    actual fun error(tag: String, t: Throwable?, message: String?) {

        if (message != null) {
            println(buildMessage(tag, message, ERROR_TXT))
        }
        t?.printStackTrace()
    }
}

private const val VERBOSE_TXT = "💜 VERBOSE"
private const val DEBUG_TXT = "💚 DEBUG"
private const val INFO_TXT = "💙 INFO"
private const val WARNING_TXT = "💛 WARN"
private const val ERROR_TXT = "❤️ ERROR"