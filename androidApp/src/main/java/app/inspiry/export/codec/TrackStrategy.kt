package app.inspiry.export.codec

import android.media.MediaFormat

interface TrackStrategy {
    /**
     * Implementation should check input media formats (can be empty)
     * and return output format
     */
    fun createTrackStrategy(input: List<MediaFormat>): MediaFormat
}