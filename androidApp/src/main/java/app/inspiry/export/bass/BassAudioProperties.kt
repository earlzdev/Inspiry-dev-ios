package app.inspiry.export.bass

data class BassAudioProperties(val sampleRate: Int,
                               val channelCount: Int,
                                 // in bits per second
                               val bitrate: Int?) {
    override fun toString(): String {
        return "AudioProperties(sampleRate=$sampleRate, channelCount=$channelCount, bitrate=$bitrate)"
    }
}