package app.inspiry.core.log

expect object PlatformLogger {

    fun verbose(tag: String, message: String)
    fun debug(tag: String, message: String)
    fun info(tag: String, message: String)
    fun warning(tag: String, message: String)
    fun error(tag: String, t: Throwable? = null, message: String?)
}