package app.inspiry.core.data

data class OriginalAudioData(
    val audioTracks: List<OriginalAudioTrack>,
    val totalDurationUs: Long,
    val totalVolume: Float
) {
    override fun toString(): String {
        return "OriginalAudioData(audioTracks=$audioTracks, totalDurationUs=$totalDurationUs, totalVolume=$totalVolume)"
    }
}


//durationUs can be bigger than actual original source file duration. Then we see if it is looped.
data class OriginalAudioTrack(
    val viewStartPositionUs: Long,
    val viewDurationUs: Long,
    val contentOffsetUs: Long,
    val path: String,
    val isLooped: Boolean,
    val volume: Float
) {
    val contentEndUs: Long
        get() = viewDurationUs + contentOffsetUs - viewStartPositionUs

    override fun toString(): String {
        return "OriginalAudioTrack(viewStartPositionUs=$viewStartPositionUs, viewDurationUs=$viewDurationUs, contentOffsetUs=$contentOffsetUs, path='$path', isLooped=$isLooped, volume=$volume)"
    }
}