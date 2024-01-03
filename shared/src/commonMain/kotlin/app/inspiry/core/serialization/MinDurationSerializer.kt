package app.inspiry.core.serialization

import app.inspiry.core.media.Media
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.*

object MinDurationSerializer : JsonTransformingSerializer<Int>(Int.serializer()) {

    private const val AS_TEMPLATE_KEY = "as_template"

    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element.jsonPrimitive.isString) {

            val str = element.jsonPrimitive.content
            val newVal = when (str) {
                AS_TEMPLATE_KEY -> Media.MIN_DURATION_AS_TEMPLATE
                else -> throw IllegalArgumentException("unknown minDuration string ${str}")
            }

            return JsonPrimitive(newVal)
        }
        return element
    }

    override fun transformSerialize(element: JsonElement): JsonElement {
        if (element is JsonPrimitive && element.jsonPrimitive.intOrNull == Media.MIN_DURATION_AS_TEMPLATE)
            return JsonPrimitive(AS_TEMPLATE_KEY)
        return element
    }
}
