package app.inspiry.textanim

import app.inspiry.core.media.Media
import dev.icerock.moko.resources.AssetResource

class MediaWithRes(val media: Media, val res: AssetResource) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MediaWithRes) return false

        if (res != other.res) return false

        return true
    }

    override fun hashCode(): Int {
        return res.hashCode()
    }
}