package app.inspiry.export.bass

import android.content.ContentResolver
import android.net.Uri
import app.inspiry.core.data.OriginalAudioTrack
import app.inspiry.core.util.getScheme
import app.inspiry.core.util.removeScheme
import com.un4seen.bass.BASS
import kotlin.math.roundToInt

class BassAudioInput(private val audioTrack: OriginalAudioTrack,
                     contentResolver: ContentResolver) {

    val chan: Int

    init {
        val path = audioTrack.path
        val scheme = path.getScheme()

        val flags = if (audioTrack.isLooped) {
            BASS.BASS_SAMPLE_LOOP or BASS.BASS_STREAM_DECODE
        } else {
            BASS.BASS_STREAM_DECODE
        }

        chan = if (scheme == "file" || scheme.isNullOrEmpty())
            BASS.BASS_StreamCreateFile(
                path.removeScheme(), 0,
                0, flags
            )
        else if (scheme == "content") {
            val descriptor = contentResolver.openFileDescriptor(Uri.parse(path), "r")
            BASS.BASS_StreamCreateFile(descriptor, 0, 0, flags)
        } else throw IllegalStateException("the path has unknown scheme $path")

        if (chan == 0) {
            throw BassException(BASS.BASS_ErrorGetCode(), "couldn\'t open file at path $path")
        }

        seek()
    }

    fun getAudioProperties(floatValue: BASS.FloatValue, info: BASS.BASS_CHANNELINFO): BassAudioProperties {

        floatValue.value = 0f
        BASS.BASS_ChannelGetAttribute(chan, BASS.BASS_ATTRIB_BITRATE, floatValue)
        val bitrate = (floatValue.value * 1000f).roundToInt()

        BASS.BASS_ChannelGetInfo(chan, info)

        return BassAudioProperties(info.freq, info.chans, bitrate)
    }

    fun getViewStartBytes(): Long {
        if (audioTrack.viewStartPositionUs == 0L) return 0L
        return BASS.BASS_ChannelSeconds2Bytes(chan, audioTrack.viewStartPositionUs.usToSeconds())
    }

    fun getViewLengthBytes(totalDuration: Long): Long {
        // we still want to limit the playback even if it is looped.
        // imagine we have a view with time start and end. The video can be shorter and looped inside.
        return if (audioTrack.viewDurationUs >= totalDuration) 0L
        else BASS.BASS_ChannelSeconds2Bytes(
            chan,
            audioTrack.viewDurationUs.usToSeconds()
        )
    }

    private fun seek() {

        if (audioTrack.isLooped) {

            BASS.BASS_ChannelSetPosition(
                chan,
                BASS.BASS_ChannelSeconds2Bytes(chan, audioTrack.contentOffsetUs.usToSeconds()),
                BASS.BASS_POS_LOOP
            )
            BASS.BASS_ChannelSetPosition(
                chan, BASS.BASS_ChannelSeconds2Bytes(chan, audioTrack.contentEndUs.usToSeconds()),
                BASS.BASS_POS_END
            )
        }

        if (audioTrack.contentOffsetUs != 0L) {
            BASS.BASS_ChannelSetPosition(
                chan,
                BASS.BASS_ChannelSeconds2Bytes(chan, audioTrack.contentOffsetUs.usToSeconds()),
                // this flag is necessary to make seek synchronous, because it is async by default.
                BASS.BASS_POS_SCAN
            )
        }
    }

    fun release() {
        BASS.BASS_StreamFree(chan)
    }
}

fun Long.usToSeconds() = this / 1000000.0