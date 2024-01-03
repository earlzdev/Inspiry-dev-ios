package app.inspiry.removebg

import app.inspiry.core.data.Size

interface RemoveBgProcessor {

    /**
     * @return size of new image
     */
    suspend fun removeBg(originalFile: String, saveToFile: String): Size
    fun getSizeOfExistingFile(file: String): Size

    fun determineFormat(originalFile: String): Format

    companion object {
        const val ENDPOINT = "https://sdk.photoroom.com/v1/segment"
        const val MAX_SIZE = 1600
    }

    enum class Format {
        png, jpg
    }
}