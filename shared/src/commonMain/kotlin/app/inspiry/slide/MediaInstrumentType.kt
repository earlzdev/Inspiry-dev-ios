package app.inspiry.slide

import app.inspiry.MR
import app.inspiry.views.media.InspMediaView
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.flow.MutableStateFlow

enum class MediaInstrumentType {
    ANIMATION, TRIM, VOLUME, REMOVE_BG, REPLACE, COLOR, MOVE, SHAPE, CROP, SLIDE
}

fun MediaInstrumentType.forPremium(): Boolean {
    return this == MediaInstrumentType.REMOVE_BG
}

fun InspMediaView.getAvailableInstrumentTypes(): List<MediaInstrumentType> {
    if (media.isVideo) return getAvailableTypesForVideo()
    else return getAvailableTypesForImage()
}

fun InspMediaView.getAvailableTypesForImage(): List<MediaInstrumentType> {
    val list = mutableListOf<MediaInstrumentType>()
    if (canRemoveBg()) {
        list.add(MediaInstrumentType.REMOVE_BG)
    }
    list.add(MediaInstrumentType.REPLACE)
    if (isInSlides()) list.add(MediaInstrumentType.SLIDE)
    list.add(MediaInstrumentType.SHAPE)
    if (media.isLogo) list.add(MediaInstrumentType.MOVE)
    if (!media.isColorFilterDisabled()) list.add(MediaInstrumentType.COLOR)

    return list
}

fun InspMediaView.getAvailableTypesForVideo(): List<MediaInstrumentType> {
    val list = mutableListOf<MediaInstrumentType>()
    list.add(MediaInstrumentType.REPLACE)
    if (isInSlides()) list.add(MediaInstrumentType.SLIDE)
    list.add(MediaInstrumentType.SHAPE)
    val videoHasAudio = this.isVideoHasAudio(true)
    val hasVideoVolumeVariable = this.getVideoVolumeConsiderDuplicate() != null

    //if (this.canTrimVideo()) { //trim enabled for any size videos
        list.add(MediaInstrumentType.TRIM)
    //}
    if (videoHasAudio) {
        list.add(MediaInstrumentType.VOLUME)
    }
    if (media.isLogo) list.add(MediaInstrumentType.MOVE)
    if (videoHasAudio != hasVideoVolumeVariable) {
        // this happens when we inserted newly created video without audio.
        // therefore mediaImage.videoVolume was created, but we don't have audio.
        // it is perfectly fine.
    }

    if (videoHasAudio && !hasVideoVolumeVariable) {
        val isVideo = isVideo()
        // I have no idea how can happen that the video has no videoVolume.
        // videoVolume is initialized when video is inserted.


        // video has audio, but mediaImage.videoVolume is null. isVideo true, viewHasVariable null, roleModelVolume null
        val exception = IllegalStateException(
            "video has audio, but mediaImage.videoVolume is null." +
                    " isVideo ${isVideo}, viewHasVariable ${media.videoVolume?.value}," +
                    " roleModelVolume ${findRoleModelMedia()?.media?.videoVolume?.value}"
        )
        logger.error(exception)

        val roleModel = findRoleModelMedia()
        (roleModel ?: this).media.videoVolume = MutableStateFlow(1f)
    }

    return list
}

fun MediaInstrumentType.text(): StringResource {
    return when (this) {
        MediaInstrumentType.REPLACE -> MR.strings.instrument_replace
        MediaInstrumentType.REMOVE_BG -> MR.strings.instrument_remove
        MediaInstrumentType.CROP -> MR.strings.instrument_crop
        MediaInstrumentType.SHAPE -> MR.strings.instrument_shape
        MediaInstrumentType.MOVE -> MR.strings.instrument_move
        MediaInstrumentType.COLOR -> MR.strings.instrument_color
        MediaInstrumentType.ANIMATION -> MR.strings.instrument_text_animation
        MediaInstrumentType.TRIM -> MR.strings.instrument_cut
        MediaInstrumentType.VOLUME -> MR.strings.instrument_page_volume
        MediaInstrumentType.SLIDE -> MR.strings.instrument_slide
    }
}

fun MediaInstrumentType.icon(): String {
    return when (this) {
        MediaInstrumentType.REPLACE -> "ic_replace"
        MediaInstrumentType.REMOVE_BG -> "ic_remove_background"
        MediaInstrumentType.CROP -> "ic_crop"
        MediaInstrumentType.SHAPE -> "ic_shape"
        MediaInstrumentType.MOVE -> "ic_animation_panel_slide"
        MediaInstrumentType.COLOR -> "ic_instrument_color"
        MediaInstrumentType.ANIMATION -> "ic_animation_panel_slide"
        MediaInstrumentType.TRIM -> "ic_trim_panel_slide"
        MediaInstrumentType.VOLUME -> "ic_volume_panel_slide"
        MediaInstrumentType.SLIDE -> "ic_instrument_slide"
    }
}