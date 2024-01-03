package app.inspiry.core.util

import okhttp3.internal.closeQuietly
import okio.buffer
import okio.sink
import java.io.OutputStream

fun String.writeToStream(os: OutputStream) {
    os.sink().buffer().writeUtf8(this).closeQuietly()
}