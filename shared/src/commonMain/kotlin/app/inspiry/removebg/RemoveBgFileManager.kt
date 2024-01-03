package app.inspiry.removebg

import app.inspiry.core.manager.DefaultDirs
import app.inspiry.core.util.removeScheme
import com.soywiz.klock.DateTime
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

object RemoveBgFileManager {

    private fun removeBgFolder(): Path {
        return DefaultDirs.contentsDirectory!!.toPath().resolve("remove-bg")
    }

    // output file is always png
    fun generateRemovedBgFile(fileSystem: FileSystem, plusIndex: Int? = null): String {

        val folder = removeBgFolder()
        val time = DateTime.nowUnixLong()

        val childName = time.toString() + (plusIndex?.toString() ?: "") + "." + RemoveBgProcessor.Format.png.name

        fileSystem.createDirectories(folder)

        return folder.resolve(childName).toString()
    }

    fun isRemovedBgFile(file: String): Boolean {

        val folder = removeBgFolder()
        return file.removeScheme().startsWith(folder.toString())
    }
}