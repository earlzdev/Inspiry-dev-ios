package app.inspiry.core.template

import app.inspiry.core.media.Media

interface MediaIdGenerator {
    fun fillEmptyID(medias: List<Media>, prefix: String = GENERATED_ID_PREFIX)

    companion object {
        const val GENERATED_ID_PREFIX = "_AUTO_ASSIGNED_ID_"
        const val SLIDES_PARENT_ID_PREFIX = "_AUTO_SLIDES_"
    }
}