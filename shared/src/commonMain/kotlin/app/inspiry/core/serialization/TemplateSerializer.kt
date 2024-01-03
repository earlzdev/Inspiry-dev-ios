package app.inspiry.core.serialization

import app.inspiry.core.media.TemplateAvailability
import app.inspiry.core.media.Template
import app.inspiry.palette.model.PaletteColor
import app.inspiry.palette.model.PaletteLinearGradient
import kotlinx.serialization.json.*

object TemplateSerializer: TemplateSerializerBase(skipTemporaryMedia = true)

// todo: the flag is inaccessible in MediaGroupSerializer
open class TemplateSerializerBase(val skipTemporaryMedia: Boolean) : MapTransformDeserializer<Template>(Template.serializer()) {

    private fun changeMainColor(map: MutableMap<String, JsonElement>, mainColor: JsonObject) {
        val palette = map["palette"]
        if (palette == null) {
            map["palette"] = JsonObject(
                mapOf(
                    "mainColor" to mainColor
                )
            )
        } else {
            map["palette"] = JsonObject(
                (palette as JsonObject).toMutableMap().apply {
                    this["mainColor"] = mainColor
                }
            )
        }
    }

    override fun transformDeserializeMap(map: MutableMap<String, JsonElement>) {

        val backgroundGradient = map.get("palette")?.jsonObject?.get("backgroundGradient")
        if (backgroundGradient != null) {

            changeMainColor(
                map, JsonObject(backgroundGradient.jsonObject.toMutableMap()
                    .also {
                        it.put(
                            "type",
                            JsonPrimitive(PaletteLinearGradient.serializer().descriptor.serialName)
                        )
                    })
            )

        } else {

            val colorFilter = map.remove("backgroundColor")
            if (colorFilter != null) {

                val mainColor = JsonObject(
                    mapOf(
                        "color" to colorFilter,
                        "type" to JsonPrimitive(PaletteColor.serializer().descriptor.serialName)
                    )
                )
                changeMainColor(map, mainColor)
            }
        }

        val premium = map.remove("premium")?.jsonPrimitive?.booleanOrNull
        val forInstagramSubscribed =
            map.remove("forInstagramSubscribed")?.jsonPrimitive?.booleanOrNull

        if (premium != null || forInstagramSubscribed != null) {

            val newAvailability =
                if (premium == true) TemplateAvailability.PREMIUM else
                    if (forInstagramSubscribed == true) TemplateAvailability.INSTAGRAM_SUBSCRIBED
                    else TemplateAvailability.FREE

            map["availability"] = JsonPrimitive(newAvailability.name)
        }
    }

    fun maybeSkipTemporaryMedia(elements: MutableMap<String, JsonElement>) {

        val medias = elements["medias"]?.jsonArray?.filter { innerElement ->
            if (innerElement is JsonObject)
                innerElement.toMutableMap()["isTemporaryMedia"]?.jsonPrimitive?.boolean != true
            else true
        }?.toMutableList()

        medias?.let {
            elements["medias"] = JsonArray(it)
        }
    }

    override fun transformSerialize(element: JsonElement): JsonElement {
        if (element is JsonObject) {

            if (skipTemporaryMedia) {
                val map = element.toMutableMap()
                maybeSkipTemporaryMedia(map)
                return JsonObject(map)
            }
        }

        return element
    }
}