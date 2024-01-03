package app.inspiry.export.audio

import app.inspiry.views.template.InspTemplateView
import app.inspiry.export.record.Encoder

interface AudioEncoderFactory {
    fun createAudioEncoder(
        templateView: InspTemplateView,
        progressWeight: Float,
        onErrorHasHappened: (Exception) -> Unit
    ): Encoder?
}