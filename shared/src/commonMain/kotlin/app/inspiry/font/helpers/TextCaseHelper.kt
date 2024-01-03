package app.inspiry.font.helpers

abstract class TextCaseHelper {

    open fun allUpperCase(value: String): Boolean = value.all { !it.isLetter() || it.isUpperCase() }

    open fun allLowerCase(value: String): Boolean = value.all { !it.isLetter() || it.isLowerCase() }

    open fun isCapitalized(value: String): Boolean = value[0].isUpperCase()

    abstract fun toLowerCase(value: String): String
    abstract fun toUpperCase(value: String): String
    abstract fun capitalize(value: String): String

    fun toggleCapsMode(value: String): String {
        return when {
            allUpperCase(value) -> {
                toLowerCase(value)
            }
            allLowerCase(value) -> {
                capitalize(value)
            }
            else -> {
                toUpperCase(value)
            }
        }
    }


    fun getCurrentCapsModeForAnalytics(value: String): String {
        return when {
            allUpperCase(value) -> {
                "uppercase"
            }
            allLowerCase(value) -> {
                "lowercase"
            }
            else -> {
                "capitalize"
            }
        }
    }

    fun setCaseBasedOnOther(value: String, other: String): String {

        return if (allUpperCase(other)) {
            toUpperCase(value)
        } else if (allLowerCase(other)) {
            toLowerCase(value)
        } else if (isCapitalized(other)) {
            capitalize(value)
        } else value
    }
}