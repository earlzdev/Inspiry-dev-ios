package app.inspiry.core.serialization

import app.inspiry.core.media.Alignment
import app.inspiry.core.media.LayoutPosition
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.json.*

object LayoutPositionSerializer :
    JsonTransformingSerializer<LayoutPosition>(LayoutPosition.serializer()) {

    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("layoutPosition") {
        element<String>("width")
        element<String>("height")
        element<String?>("x")
        element<String?>("y")
        element<String?>("paddingStart")
        element<String?>("paddingEnd")
        element<String?>("paddingBottom")
        element<String?>("paddingTop")
        element<String?>("marginLeft")
        element<String?>("marginRight")
        element<String?>("marginBottom")
        element<String?>("marginTop")
        element<String?>("anchorX")
        element<String?>("anchorY")
        element<String?>("padding")
        element<Boolean>("relativeToParent")
        element<String?>("alignBy")
    }

    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element is JsonObject) {

            val map = element.toMutableMap()
            val padding = map.remove("padding")?.jsonPrimitive?.contentOrNull

            if (padding != null) {
                map["paddingStart"] = JsonPrimitive(padding)
                map["paddingTop"] = JsonPrimitive(padding)
                map["paddingBottom"] = JsonPrimitive(padding)
                map["paddingEnd"] = JsonPrimitive(padding)
            }

            val margin = map.remove("margin")?.jsonPrimitive?.contentOrNull

            if (margin != null) {
                map["marginLeft"] = JsonPrimitive(margin)
                map["marginTop"] = JsonPrimitive(margin)
                map["marginBottom"] = JsonPrimitive(margin)
                map["marginRight"] = JsonPrimitive(margin)
            }

            val anchorX: String? = map.remove("anchorX")?.jsonPrimitive?.contentOrNull
            val anchorY: String? = map.remove("anchorY")?.jsonPrimitive?.contentOrNull

            if (anchorX != null || anchorY != null) {
                val alignmentHorizontal = when (anchorX) {
                    "start", "", null -> Alignment.Horizontal.Start
                    "end" -> Alignment.Horizontal.End
                    "center" -> Alignment.Horizontal.Center
                    else -> throw IllegalArgumentException("wrong anchorX $anchorX")
                }


                val alignmentVertical = when (anchorY) {
                    "top", "", null -> Alignment.Vertical.Top
                    "bottom" -> Alignment.Vertical.Bottom
                    "center" -> Alignment.Vertical.Center
                    else -> throw IllegalArgumentException("wrong anchorY $anchorY")
                }

                val alignment: Alignment = alignmentVertical + alignmentHorizontal
                map["alignBy"] = JsonPrimitive(alignment.name)
            }

            return JsonObject(map)
        }
        return element
    }
}