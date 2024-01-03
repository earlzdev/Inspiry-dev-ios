package app.inspiry.core.serialization

import app.inspiry.core.animator.InspAnimator
import app.inspiry.core.animator.appliers.AnimApplier
import app.inspiry.core.animator.appliers.FadeAnimApplier
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

object AnimatorSerializer : KSerializer<InspAnimator> {

    private const val ANIM_APPLIER = "animationApplier"
    private val tSerializer = InspAnimator.serializer()


    override fun serialize(encoder: Encoder, value: InspAnimator) {
        val output = encoder as JsonEncoder
        var element =  output.json.encodeToJsonElement(tSerializer, value)
        element = transformSerialize(element)
        output.encodeJsonElement(element)
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): InspAnimator {
        val input = decoder as JsonDecoder
        val element = input.decodeJsonElement()

        if (element is JsonObject) {

            val map: MutableMap<String, JsonElement> = element.toMutableMap()

            map.deserializeMillis("startTimeMillis", "startFrame")
            map.deserializeMillisMayString("durationMillis", "duration")
            val durationJson = map["duration"]

            val startFrame = map["startFrame"]?.jsonPrimitive?.intOrNull ?: 0
            val duration: Int = if (durationJson == null) 0 else
                decoder.json.decodeFromJsonElement(AnimatorDurationSerializer, durationJson)
            val interpolatorElement = map["interpolator"]

            descriptor.elementNames.forEach {
                map.remove(it)
            }
            val animApplierElement = JsonObject(map)

            val interpolator = interpolatorElement?.let { decoder.json.decodeFromJsonElement(InterpolatorSerializer, it) }
            val animApplier = decoder.json.decodeFromJsonElement(AnimApplier.serializer(), animApplierElement)

            return InspAnimator(startFrame, duration, interpolator, animApplier)
        } else
            throw IllegalStateException()

    }

    private fun transformSerialize(element: JsonElement): JsonElement {

        if (element is JsonObject) {

            val map = element.toMutableMap()
            map.unwrapNestedElement(ANIM_APPLIER)

            return JsonObject(map)
        }
        return element
    }

    override val descriptor: SerialDescriptor
        get() = tSerializer.descriptor
}