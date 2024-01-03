package app.inspiry.helpers

import android.content.Context
import app.inspiry.core.manager.FileReadWrite
import app.inspiry.core.util.removeScheme
import app.inspiry.core.util.writeToStream
import dev.icerock.moko.resources.AssetResource
import okio.buffer
import okio.source
import java.io.File

class FileReadWriteAndroid(val context: Context): FileReadWrite {

    override fun writeContentToFile(str: String, path: String) {
        str.writeToStream(File(path).outputStream())
    }

    override fun readContentFromFiles(path: String): String {
        return File(path.removeScheme()).source().buffer().readUtf8()
    }

    override fun readContentFromAssets(path: String): String {
        return context.assets.open(path.removeScheme()).source().buffer().readUtf8()
    }

    override fun readContentFromAssets(asset: AssetResource): String {
        return context.assets.open(asset.path).source().buffer().readUtf8()
    }
}