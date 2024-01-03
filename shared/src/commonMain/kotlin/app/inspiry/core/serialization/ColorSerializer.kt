package app.inspiry.core.serialization

import app.inspiry.core.util.ArgbColorManager
import app.inspiry.core.util.parseColor
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

object ColorSerializer : KSerializer<Int> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("ColorSerialization", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Int) {
        val string = ArgbColorManager.colorToString(value)
        encoder.encodeString(string)
    }

    override fun deserialize(decoder: Decoder): Int {
        val string = decoder.decodeString()
        return string.parseColor()
    }
}