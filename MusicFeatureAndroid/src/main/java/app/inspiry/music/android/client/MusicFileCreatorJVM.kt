package app.inspiry.music.android.client

import app.inspiry.core.util.getFileName
import app.inspiry.music.client.MusicFileCreator
import okio.FileSystem
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

class MusicFileCreatorJVM(fileSystem: FileSystem) : MusicFileCreator(fileSystem) {
    override fun getBaseName(url: String): String {
        return URLDecoder.decode(url.getFileName(), StandardCharsets.UTF_8.name())
    }
}