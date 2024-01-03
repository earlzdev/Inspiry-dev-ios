package app.inspiry.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import app.inspiry.core.data.OriginalTemplateData
import app.inspiry.core.data.TemplatePath
import app.inspiry.export.PredefineAppProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import java.io.File


object IntentUtils {

    fun openLink(url: String): Intent {
        var url = url
        // if protocol isn't defined use http by default
        if (!TextUtils.isEmpty(url) && !url.contains("://")) {
            url = "http://$url"
        }

        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.data = Uri.parse(url)
        return intent
    }
}

fun Intent.getOriginalTemplateData(): OriginalTemplateData? {
    return getParcelable(Constants.EXTRA_ORIGINAL_DATA)
}

fun Intent.putOriginalTemplateData(data: OriginalTemplateData?): Intent {
    return putParcelable(data, Constants.EXTRA_ORIGINAL_DATA)
}

fun Intent.getTemplatePath(): TemplatePath {
    return getParcelable(Constants.EXTRA_TEMPLATE_PATH)!!
}

fun Intent.putTemplatePath(data: TemplatePath): Intent {
    return putParcelable(data, Constants.EXTRA_TEMPLATE_PATH)
}

private fun <Item : Parcelable> Intent.putParcelable(data: Item?, key: String): Intent {
    if (data != null) {
        val b = Bundle()
        b.putParcelable(key, data)
        putExtra(key, b)
    }
    return this
}

private fun <Item : Parcelable> Intent.getParcelable(key: String): Item? {
    val b = getBundleExtra(key)
    return b?.getParcelable(key)
}

fun Activity.sendEmail(address: String, subject: String? = null, body: String? = null) {
    val i = Intent(Intent.ACTION_SENDTO)
    i.type = "message/rfc822"


    val bodyAppend =
        "Android Version ${Build.VERSION.SDK_INT}. Phone model ${Build.BRAND} ${Build.MODEL}, " +
                "Inspiry version v${appVersion()} b${appBuildNumber(packageName)}"


    var subject = subject
    if (subject == null) subject = "Inspiry feedback"

    val newBody: String = if (body.isNullOrBlank()) bodyAppend
    else body + "\n\n" + bodyAppend


    val uriText = "mailto:${address}?subject=${subject}&body=${newBody}"

    i.data = Uri.parse(uriText)

    try {
        startActivity(Intent.createChooser(i, "Send mail..."))
    } catch (ex: ActivityNotFoundException) {
        Toast.makeText(this, "Function is not available", Toast.LENGTH_SHORT).show()
    }
}

fun File.getUriForIntent(context: Context): Uri = FileProvider.getUriForFile(
    context,
    "${context.packageName}.helpers.GenericFileProvider.all",
    this
)


private fun Activity.getIntentBuilderShareMedia(
    file: File,
    mimeType: String
): ShareCompat.IntentBuilder {
    val mediaUri: Uri = file.getUriForIntent(this)
    return ShareCompat.IntentBuilder(this)
        .setStream(mediaUri)
        .setType(mimeType)
        .setChooserTitle("Share media...")
}

fun Activity.shareMediaToApp(file: File, mimeType: String, appPackage: String) {
    startActivity(
        getIntentBuilderShareMedia(file, mimeType)
            .intent.setPackage(appPackage)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    )
}

fun Activity.startActivitySafe(intent: Intent) {
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "this function is not available", Toast.LENGTH_SHORT).show()
    }
}

@SuppressLint("CheckResult")
fun Activity.nameYourStoryDialog(prefill: CharSequence?, onInput: (CharSequence?) -> Unit) {
    MaterialDialog(this).show {
        title(text = getString(app.inspiry.projectutils.R.string.dialog_name_title))
        input(maxLength = 50, allowEmpty = true, prefill = prefill) { dialog, text ->
            onInput(text)
        }
        positiveButton(text = getString(app.inspiry.projectutils.R.string.dialog_name_submit))
    }
}


val POPULAR_SHARE_PACKAGES = setOf(
    PredefineAppProvider.instagramPackage,
    PredefineAppProvider.whatsappPackage,
    PredefineAppProvider.facebookPackage,
    "com.gbwhatsapp",
    "org.telegram.messenger",
    "com.viber.voip",
    "com.vkontakte.android",
    "com.snapchat.android",
    "com.zhiliaoapp.musically"
)