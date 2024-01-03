package app.inspiry.core.helper

expect object KotlinFormatter {
    fun format(text: String, vararg args: Any?): String
}