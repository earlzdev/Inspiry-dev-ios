package app.inspiry.core.manager

import android.content.Context
import java.util.concurrent.atomic.AtomicReference

actual object DefaultDirs {
    private var context = AtomicReference<Context>(null)

    fun initialize(context: Context?) {
        this.context.set(context)
    }

    actual val cachesDirectory: String?
        get() = context.get()!!.cacheDir.absolutePath
    actual val contentsDirectory: String?
        get() = context.get()!!.filesDir.absolutePath

}