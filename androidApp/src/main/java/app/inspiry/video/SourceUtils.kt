package app.inspiry.video

import android.content.Context
import android.content.res.AssetFileDescriptor
import app.inspiry.core.util.parseAssetsPath
import okio.buffer
import okio.source

object SourceUtils {

    fun getAssetFileDescriptor(path: String, context: Context): AssetFileDescriptor? {
        val assetsPath = path.parseAssetsPath() ?: return null
        return context.assets.openFd(assetsPath)
    }

    fun readTextFromAssets(path: String, context: Context): String? {
        val assetsPath = path.parseAssetsPath() ?: return null
        val inputStream = context.assets.open(assetsPath)
        return inputStream.source().buffer().readUtf8()
    }

}

fun String.parseAssetsPathForAndroid(): String = this.replace("assets://", "file:///android_asset/")