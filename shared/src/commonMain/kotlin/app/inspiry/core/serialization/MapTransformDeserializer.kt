package app.inspiry.core.serialization

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonTransformingSerializer

abstract class MapTransformDeserializer<T : Any>(tSerializer: KSerializer<T>) :
    JsonTransformingSerializer<T>(tSerializer) {
    abstract fun transformDeserializeMap(map: MutableMap<String, JsonElement>)

    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element is JsonObject) {

            val map = element.toMutableMap()

            transformDeserializeMap(map)

            return JsonObject(map)
        }
        return element
    }
}