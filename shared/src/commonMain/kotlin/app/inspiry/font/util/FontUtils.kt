package app.inspiry.font.util

object FontUtils {

    private const val cyrillicChars =
        "абвгдеёжзийклмнопрстуфхцчшщъыьэюяАБВГДЕËЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ"

    fun hasCyrillic(charSequence: CharSequence) =
        charSequence.any { it in cyrillicChars }


    fun isFont(font: String) =
        font.endsWith(".ttf") || font.endsWith(".otf") || font.endsWith(".woff") || font.endsWith(
            ".woff2"
        )
}


fun <K, V> Map<K, V>.reversedMap(): Map<V, K> {
    val newMap = HashMap<V, K>()

    forEach { entry ->
        newMap[entry.value] = entry.key
    }

    return newMap
}

