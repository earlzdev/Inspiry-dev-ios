package app.inspiry.music.client

import app.inspiry.core.manager.DefaultDirs
import app.inspiry.core.util.appendExt
import app.inspiry.core.util.getExt
import com.soywiz.klock.DateTime
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath

open class MusicFileCreator(val fileSystem: FileSystem) {

    private fun musicFolder(folder: String): Path {

        val path = DefaultDirs.contentsDirectory!!.toPath().resolve("music")
        val folderPath = path.resolve(folder)
        fileSystem.createDirectories(folderPath)
        return folderPath
    }

    open fun getBaseName(url: String): String {
        return DateTime.nowUnixLong().toString().appendExt(url.getExt())
    }

    fun getDownloadFile(url: String, folder: String): Path {

        val parent = musicFolder(folder)

        val baseName = getBaseName(url)

        return parent.resolve(baseName)
    }
}