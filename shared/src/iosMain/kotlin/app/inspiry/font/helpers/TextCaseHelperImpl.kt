package app.inspiry.font.helpers


class TextCaseHelperImpl() : TextCaseHelper() {

    override fun toLowerCase(value: String): String {
        return value.lowercase()
    }

    override fun toUpperCase(value: String): String {
        return value.uppercase()
    }

    override fun capitalize(value: String): String {
        return value.lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

}