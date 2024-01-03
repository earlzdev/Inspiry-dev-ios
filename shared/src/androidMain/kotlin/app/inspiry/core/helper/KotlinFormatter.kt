package app.inspiry.core.helper

actual object KotlinFormatter {
    actual fun format(text: String, vararg args: Any?): String {
        return java.lang.String.format(text, *args)
    }
}