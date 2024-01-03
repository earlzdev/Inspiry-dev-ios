package app.inspiry.core.serialization

import app.inspiry.core.animator.interpolator.InspInterpolator
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

object InterpolatorSerializer : KSerializer<InspInterpolator> {

    private val originalSerializer = InspInterpolator.serializer()

    override fun deserialize(decoder: Decoder): InspInterpolator {
        val input = decoder as JsonDecoder
        val element = input.decodeJsonElement()

        if (element is JsonPrimitive) {
            return InspInterpolator.pathInterpolatorBy(element.jsonPrimitive.content)
        }

        return decoder.json.decodeFromJsonElement(originalSerializer, element)
    }

    override val descriptor: SerialDescriptor
        get() = originalSerializer.descriptor

    override fun serialize(encoder: Encoder, value: InspInterpolator) {

        val enc = encoder as JsonEncoder
        val element = enc.json.encodeToJsonElement(InspInterpolator.serializer(), value)
        return enc.encodeJsonElement(element)
    }
}