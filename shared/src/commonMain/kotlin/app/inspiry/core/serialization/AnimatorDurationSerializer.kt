package app.inspiry.core.serialization

import app.inspiry.core.animator.InspAnimator
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.*

object AnimatorDurationSerializer : JsonTransformingSerializer<Int>(Int.serializer()) {

    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element.jsonPrimitive.isString) {

            val str = element.jsonPrimitive.content
            val newVal = when (str) {
                "all" -> InspAnimator.DURATION_ALL
                "in" -> InspAnimator.DURATION_IN
                "as_template" -> InspAnimator.DURATION_AS_TEMPLATE
                else -> throw IllegalStateException("unknown animator duration string ${str}")
            }

            return JsonPrimitive(newVal)
        }
        return element
    }

    override fun transformSerialize(element: JsonElement): JsonElement {

        val str = when (element.jsonPrimitive.intOrNull) {
            InspAnimator.DURATION_ALL -> "all"
            InspAnimator.DURATION_AS_TEMPLATE -> "as_template"
            InspAnimator.DURATION_IN -> "in"
            else -> null
        }
        if (str != null) return JsonPrimitive(str)

        return element
    }
}
