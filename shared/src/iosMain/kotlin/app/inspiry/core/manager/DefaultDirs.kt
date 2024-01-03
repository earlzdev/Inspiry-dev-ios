package app.inspiry.core.manager

import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.Foundation.*

actual object DefaultDirs {

    actual val cachesDirectory: String?
        get() = getDirPath(NSCachesDirectory, true)
    actual val contentsDirectory: String?
        get() = getDirPath(NSDocumentDirectory, true)


    private fun getDirPath(directory: NSSearchPathDirectory, create: Boolean = false): String? {
        return getDirUrl(directory, create)?.path
    }

    private val manager = NSFileManager.defaultManager

    private fun getDirUrl(directory: NSSearchPathDirectory, create: Boolean = false): NSURL? {
        memScoped {
            val error = alloc<ObjCObjectVar<NSError?>>()
            return manager.URLForDirectory(directory, NSUserDomainMask, null, create, error.ptr)?.standardizedURL
        }
    }
}