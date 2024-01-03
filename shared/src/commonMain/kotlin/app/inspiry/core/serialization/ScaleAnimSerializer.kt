package app.inspiry.core.serialization

import app.inspiry.core.animator.appliers.ScaleOuterAnimApplier
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

object ScaleAnimSerializer : KSerializer<ScaleOuterAnimApplier> {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("scale") {
        element<Float?>("from")
        element<Float?>("to")
        element<Float?>("fromX")
        element<Float?>("toX")
        element<Float?>("fromY")
        element<Float?>("toY")
    }

    override fun serialize(encoder: Encoder, value: ScaleOuterAnimApplier) {
        encoder.encodeStructure(descriptor) {
            encodeFloatElement(descriptor, 2, value.fromX)
            encodeFloatElement(descriptor, 3, value.toX)
            encodeFloatElement(descriptor, 4, value.fromY)
            encodeFloatElement(descriptor, 5, value.toY)
        }
    }

    override fun deserialize(decoder: Decoder): ScaleOuterAnimApplier {

        // Cast to JSON-specific interface
        val jsonDecoder = decoder as? JsonDecoder ?: error("Can be deserialized only by JSON")
        // Read the whole content as JSON
        val obj = jsonDecoder.decodeJsonElement().jsonObject.toMutableMap()

        val from = obj["from"]?.jsonPrimitive?.floatOrNull
        val to = obj["to"]?.jsonPrimitive?.floatOrNull

        fun getValue(main: Float?, fallback: String, default: Float = 1f) =
            main ?: obj[fallback]?.jsonPrimitive?.floatOrNull ?: default


        return ScaleOuterAnimApplier(getValue(from, "fromX"), getValue(to, "toX"),
            getValue(from, "fromY"), getValue(to, "toY"))
    }
}