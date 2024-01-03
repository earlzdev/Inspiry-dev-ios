package app.inspiry.core.manager

import dev.icerock.moko.resources.AssetResource

interface FileReadWrite {
    //return list of names in that folder.
    fun writeContentToFile(str: String, path: String)
    fun readContentFromFiles(path: String): String
    fun readContentFromAssets(path: String): String
    fun readContentFromAssets(asset: AssetResource): String
}