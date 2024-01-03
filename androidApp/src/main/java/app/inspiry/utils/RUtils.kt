package app.inspiry.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.util.TypedValue
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import app.inspiry.ap
import java.util.*


val METRICS_DENSITY by lazy { ap.resources.displayMetrics.density }

fun Int.dpToPixels() = this * METRICS_DENSITY
fun Int.dpToPxInt() = (this * METRICS_DENSITY).toInt()
fun Int.pixelsToDp() = this / METRICS_DENSITY
fun Float.pixelsToDp() = this / METRICS_DENSITY

fun Float.dpToPixels() = this * METRICS_DENSITY
fun Float.spToPixels() =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, this, ap.resources.displayMetrics)

fun Context.dpToPixels(value: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, resources.displayMetrics)
}
fun Context.spToPixels(value: Float): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, resources.displayMetrics)
}

fun Context.findResIdByName(name: String): Int {
    return this.resources.getIdentifier(name, "drawable", packageName)
}

@SuppressLint("NewApi")
fun Context.getColorCompat(@ColorRes r: Int): Int {
    return getColor(r)
}

@SuppressLint("ResourceType")
fun Context.getColorStateListCompat(@DrawableRes r: Int) =
    getColorStateList(r)


fun Context.getContextWithLocale(locale: String): Context {
    val configuration = Configuration(resources.configuration)
    configuration.setLocale(Locale(locale))
    return createConfigurationContext(configuration)
}

/**
 * Check if package installed
 *
 * @param context Context of current app
 * @param uri     Package of application to check
 * @return true if passed package installed
 */
fun Context.isAppInstalled(uri: String): Boolean {
    val pm = packageManager
    return pm.isAppInstalled(uri)
}

fun PackageManager?.isAppInstalled(uri: String): Boolean {
    return if (this == null) {
        false
    } else {
        try {
            getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }
}
fun Context.appBuildNumber(uri: String): Long {
    return try {
        val pi =
            packageManager?.getPackageInfo(uri, PackageManager.GET_ACTIVITIES)
        if (Build.VERSION.SDK_INT >= 28) {
            pi?.longVersionCode ?: 0L
        } else {
            pi?.versionCode?.toLong() ?: 0L
        }
    } catch (e: PackageManager.NameNotFoundException) {
        0L
    }
}

fun Context.appVersion(): String? {
    return try {
        val pi =
            packageManager?.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        pi?.versionName
    } catch (e: PackageManager.NameNotFoundException) {
        null
    }
}

fun Context.getIdFromAttr(attr: Int): Int {
    val typedValue = TypedValue()
    try {

        theme.resolveAttribute(attr, typedValue, true)
        return typedValue.resourceId

    } catch (t: Throwable) {
        return 0
    }
}