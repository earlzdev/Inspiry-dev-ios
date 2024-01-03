package app.inspiry.core.helper

import platform.Foundation.NSString
import platform.Foundation.stringWithFormat

actual object KotlinFormatter {
    actual fun format(text: String, vararg args: Any?): String {
        var returnString = ""
        val regEx = "%[\\d|.]*[sdf]|[%]".toRegex()
        val singleFormats = regEx.findAll(text).map {
            it.groupValues.first()
        }.asSequence().toList()
        val newStrings = text.split(regEx)
        for (i in 0 until args.count()) {
            val arg = args[i]
            returnString += when (arg) {
                is Double -> {
                    NSString.stringWithFormat(newStrings[i] + singleFormats[i], args[i] as Double)
                }
                is Int -> {
                    NSString.stringWithFormat(newStrings[i] + singleFormats[i], args[i] as Int)
                }
                else -> {
                    NSString.stringWithFormat(newStrings[i] + "%@", args[i])
                }
            }
        }
        return returnString
    }
}