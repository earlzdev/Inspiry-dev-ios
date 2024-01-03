package app.inspiry.core.template

import app.inspiry.core.log.GlobalLogger
import app.inspiry.core.manager.DebugManager
import app.inspiry.core.media.Media
import app.inspiry.core.template.MediaIdGenerator.Companion.GENERATED_ID_PREFIX
import app.inspiry.views.template.forEachRecursive

class MediaIdGeneratorImpl: MediaIdGenerator {
    /**
     * all empty ids will be replaced with GENERATED_ID_PREFIX + Index
     * eg _AUTO_ASSIGNED_ID_1
     **/
    override fun fillEmptyID(medias: List<Media>, prefix: String) {
        var index = getNextAssignID(medias, prefix)
        val ids = mutableListOf<String>()
        medias.forEachRecursive { media ->
            if (media.id == null) {
                media.id = "${prefix}$index"
                index++
            }
            ids.add(media.id!!)
        }
        if (DebugManager.isDebug) {
            val hasDoubles = ids.groupingBy { it }.eachCount().any { it.value > 1 }
            if (hasDoubles) throw IllegalStateException ("media has duplicate ids: ${ids}")
        }
    }
    fun getNextAssignID(medias: List<Media>, prefix: String = GENERATED_ID_PREFIX): Int {
        var lastIndex = 1
        medias.forEachRecursive { media ->
            val mediaId = media.id
            if (mediaId?.matches("^${prefix}[0-9]+\$".toRegex()) == true) {
                val currentIndex = mediaId.replace("[^0-9]".toRegex(), "").toIntOrNull()
                currentIndex?.let { if (it>=lastIndex) lastIndex = it+1 }
            }
        }
        return lastIndex
    }
}
