package app.inspiry.core.animator.appliers

import app.inspiry.core.media.Media

interface ChangeableAnimApplier {
    fun onValuesChanged(media: Media)
}