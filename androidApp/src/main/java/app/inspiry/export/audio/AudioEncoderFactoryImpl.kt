package app.inspiry.export.audio

import android.content.Context
import android.os.Handler
import app.inspiry.core.data.OriginalAudioData
import app.inspiry.core.data.OriginalAudioTrack
import app.inspiry.export.bass.BassAudioSource
import app.inspiry.export.codec.DefaultAudioStrategy
import app.inspiry.export.codec.MediaDecoderAudioSource
import app.inspiry.export.codec.MediaExtractorSource
import app.inspiry.export.codec.SyncToAsyncSourceWrapper
import app.inspiry.views.template.InspTemplateView
import app.inspiry.export.record.Encoder

class AudioEncoderFactoryImpl(val context: Context, val handler: Handler) : AudioEncoderFactory {

    private fun encoderTypeToUse(): AudioEncoderType {
        return AudioEncoderType.BASS_TO_ENCODER
    }

    private fun getDataToRecord(
        templateView: InspTemplateView,
        type: AudioEncoderType
    ): OriginalAudioData? {
        return if (type == AudioEncoderType.CODEC_SINGLE_AUDIO) {
            templateView.getOriginalAudioDataForRecordOnlyMusic()
        } else {
            templateView.getOriginalAudioDataForRecordAllTracks()
        }
    }

    override fun createAudioEncoder(
        templateView: InspTemplateView,
        progressWeight: Float,
        onErrorHasHappened: (Exception) -> Unit
    ): Encoder? {

        val encoderType = encoderTypeToUse()
        val dataToRecord = getDataToRecord(templateView, encoderType) ?: return null

        val audioEncoder = when (encoderType) {

            AudioEncoderType.BASS_TO_ENCODER -> {

                val dataSource = BassAudioSource(dataToRecord, context, onErrorHasHappened)
                AudioEncoder(
                    handler,
                    dataToRecord.totalDurationUs,
                    progressWeight,
                    SyncToAsyncSourceWrapper(dataSource),
                    DefaultAudioStrategy()
                )
            }
            AudioEncoderType.CODEC_SINGLE_AUDIO -> {
                createSingleEncoder(
                    dataToRecord.totalVolume,
                    progressWeight,
                    dataToRecord.totalDurationUs,
                    dataToRecord.audioTracks.first(),
                    onErrorHasHappened
                )
            }
        }

        return audioEncoder
    }

    private fun createSingleEncoder(
        totalVolume: Float,
        progressWeight: Float,
        durationUs: Long,
        audioTrack: OriginalAudioTrack,
        onErrorHasHappened: (Exception) -> Unit

    ): AudioEncoder {
        val rawDataSource = MediaExtractorSource(context, audioTrack)
        rawDataSource.start()

        val decodedSource =
            MediaDecoderAudioSource(
                handler,
                rawDataSource.getMediaFormat(),
                rawDataSource,
                onErrorHasHappened,
                totalVolume
            )

        return AudioEncoder(
            handler, durationUs, progressWeight, decodedSource, DefaultAudioStrategy(setMaxInputSize = true)
        )
    }
}
