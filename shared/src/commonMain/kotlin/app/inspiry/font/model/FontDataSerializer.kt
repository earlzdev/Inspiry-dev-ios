package app.inspiry.font.model

import kotlinx.serialization.json.*

object FontDataSerializer :
    JsonTransformingSerializer<FontData>(FontData.serializer()) {

    override fun transformDeserialize(element: JsonElement): JsonElement {

        //if fontStyle is unknown, fontStyle will be removed
        if (element is JsonObject && element["fontStyle"] != null) {
            val style = element["fontStyle"]?.jsonPrimitive?.contentOrNull

            if (enumValues<InspFontStyle>().find { it.name == style } == null) {
                val map = element.toMutableMap()
                map.remove("fontStyle")
                return JsonObject(map)
            }
        }

        if (element is JsonPrimitive) {

            return JsonObject(mapOf("fontPath" to element.jsonPrimitive))
        }
        return element
    }

    override fun transformSerialize(element: JsonElement): JsonElement {


        if (element is JsonObject && element["fontPath"] != null) {

            val style = element["fontStyle"]?.jsonPrimitive?.contentOrNull
            if (style == null || style == InspFontStyle.regular.name)
                return element["fontPath"]!!
        }

        return element
    }
}