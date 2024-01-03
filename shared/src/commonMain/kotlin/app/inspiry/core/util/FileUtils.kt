package app.inspiry.core.util

import okio.Closeable

object FileUtils {
    const val ASSETS_SCHEME = "assets"
    const val FILE_SCHEME = "file"
}
const val separatorChar = '/'

fun String.removeExt(): String {
    return substring(0, lastIndexOf('.'))
}

fun String.getFileName(): String {
    val separatorIndex = lastIndexOf(separatorChar)
    return if (separatorIndex < 0) this else substring(separatorIndex + 1, length)
}

fun String.withScheme(scheme: String): String {
    return "$scheme://$this"
}

fun String.appendExt(ext: String?): String {
    return if (ext == null) this else "$this.$ext"
}

fun String.getFileNameWithParent(): String {
    var separatorIndex = -1

    var i = length - 1

    while (i >= 0) {
        if (get(i) == separatorChar) {
            if (separatorIndex != -1) {
                separatorIndex = i
                break
            } else {
                separatorIndex = i
            }
        }
        i--
    }

    return if (separatorIndex < 0) this else substring(separatorIndex + 1, length)
}

fun String.removeScheme(): String {
    val i = indexOf("://")
    if (i >= 0) {
        return substring(i + 3)
    }
    return this
}

fun String.getScheme(): String? {
    val i = indexOf("://")
    if (i >= 0) {
        return substring(0, i)
    }
    return null
}

fun String.getExt(): String? {
    val indexOf = lastIndexOf('.')
    return if (indexOf != -1) {
        substring(indexOf + 1)
    } else null
}

fun String.hasExtension(): Boolean {
    val ext = getExt()
    return ext != null && ext.length < 5
}

//removes assets:// scheme from path.
fun String.parseAssetsPath(): String? {

    val scheme = getScheme()
    if (scheme == FileUtils.ASSETS_SCHEME) {
        val path = removeScheme()
        if (path.startsWith('/'))
            return path.substring(1)
        return path
    } else {
        //legacy for android
        if (scheme == "file") {
            val newPath = this.substringAfter("file:///android_asset/", missingDelimiterValue = "")
            if (newPath.isNotEmpty()) return newPath
        }
    }


    return null
}

fun Closeable.closeQuietly() {
    try {
        close()
    } catch (ignored: Throwable) {

    }
}