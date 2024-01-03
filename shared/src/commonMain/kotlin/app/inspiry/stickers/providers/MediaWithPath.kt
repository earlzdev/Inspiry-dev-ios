package app.inspiry.stickers.providers

import app.inspiry.core.media.Media

class MediaWithPath(val media: Media, val path: String, val changeLoopStateBeforeSaving: Boolean = false)