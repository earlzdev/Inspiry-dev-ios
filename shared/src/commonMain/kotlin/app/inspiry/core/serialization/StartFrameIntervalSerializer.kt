package app.inspiry.core.serialization

import app.inspiry.core.media.SlidesData.Companion.START_FRAME_INTERVAL_WHEN_NEXT_OUT_BEGINS
import app.inspiry.core.media.SlidesData.Companion.START_FRAME_INTERVAL_WHEN_THIS_OUT_ENDS
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.*

object StartFrameIntervalSerializer : JsonTransformingSerializer<Int>(Int.serializer()) {

    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element.jsonPrimitive.isString) {

            val newVal = when (val str = element.jsonPrimitive.content) {
                KEY_NEXT_OUT_BEGINS -> START_FRAME_INTERVAL_WHEN_NEXT_OUT_BEGINS
                KEY_THIS_OUT_ENDS -> START_FRAME_INTERVAL_WHEN_THIS_OUT_ENDS
                else -> throw IllegalArgumentException("unknown minDuration string $str")
            }

            return JsonPrimitive(newVal)
        }
        return element
    }

    override fun transformSerialize(element: JsonElement): JsonElement {
        if (element is JsonPrimitive) {
            val intVal = element.jsonPrimitive.intOrNull

            if (intVal == START_FRAME_INTERVAL_WHEN_THIS_OUT_ENDS) {
                return JsonPrimitive(KEY_THIS_OUT_ENDS)
            } else if (intVal == START_FRAME_INTERVAL_WHEN_NEXT_OUT_BEGINS) {
                return JsonPrimitive(KEY_NEXT_OUT_BEGINS)
            }
        }
        return element
    }
}

private const val KEY_NEXT_OUT_BEGINS = "next_out_begins"
private const val KEY_THIS_OUT_ENDS = "this_out_ends"