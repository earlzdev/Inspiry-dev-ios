package app.inspiry.core.serialization

import app.inspiry.core.data.FRAME_IN_MILLIS
import app.inspiry.core.data.TouchAction
import app.inspiry.core.media.*
import app.inspiry.palette.model.PaletteColor
import app.inspiry.views.InspView
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.*
import kotlin.math.roundToInt

object MediaPathSerializer : BaseMediaSerializer<MediaPath>(MediaPath.serializer())
object MediaImageSerializer : BaseMediaSerializer<MediaImage>(MediaImage.serializer())

object MediaGroupSerializer : BaseMediaSerializer<MediaGroup>(MediaGroup.serializer()) {

    override fun transformSerializeMap(map: MutableMap<String, JsonElement>) {
        super.transformSerializeMap(map)
        TemplateSerializer.maybeSkipTemporaryMedia(map)
    }
}

object MediaTextureSerializer : BaseMediaSerializer<MediaTexture>(MediaTexture.serializer()) {
    override fun transformDeserializeMap(map: MutableMap<String, JsonElement>) {
        if (map["width"]?.jsonPrimitive?.content.isNullOrEmpty()) map["width"] =
            JsonPrimitive("take_from_media")
        if (map["height"]?.jsonPrimitive?.content.isNullOrEmpty()) map["height"] =
            JsonPrimitive("take_from_media")
        super.transformDeserializeMap(map)
    }

    override fun transformSerializeMap(map: MutableMap<String, JsonElement>) {
        if (map["width"]?.jsonPrimitive?.content == "take_from_media") map.remove("width")
        if (map["height"]?.jsonPrimitive?.content == "take_from_media") map.remove("height")
        super.transformSerializeMap(map)
    }
}

object MediaVectorSerializer : BaseMediaSerializer<MediaVector>(MediaVector.serializer()) {
    override fun transformDeserializeMap(map: MutableMap<String, JsonElement>) {
        super.transformDeserializeMap(map)

        val colorFilter = map.remove("colorFilter")
        if (colorFilter != null) {
            map["mediaPalette"] = JsonObject(
                mapOf(
                    "mainColor" to
                            JsonObject(
                                mapOf(
                                    "color" to colorFilter,
                                    "type" to JsonPrimitive(PaletteColor.serializer().descriptor.serialName)
                                )
                            )
                )
            )
        }

        val keyStaticFrameForEdit = "staticFrameForEdit"
        val staticFrameForEdit = map.get(keyStaticFrameForEdit)
        if (staticFrameForEdit?.jsonPrimitive?.isString == true) {
            map.remove(keyStaticFrameForEdit)

            val newValue: Int = when (staticFrameForEdit.jsonPrimitive.content) {
                "last" -> MediaVector.STATIC_FRAME_FOR_EDIT_LAST
                "middle" -> MediaVector.STATIC_FRAME_FOR_EDIT_MIDDLE
                else -> throw IllegalStateException("unknown string static frame for edit ${staticFrameForEdit.jsonPrimitive}")
            }
            map[keyStaticFrameForEdit] = JsonPrimitive(newValue)
        }
    }

    override fun transformSerializeMap(map: MutableMap<String, JsonElement>) {
        super.transformSerializeMap(map)

        val keyStaticFrameForEdit = "staticFrameForEdit"
        val staticFrameForEdit = map.get(keyStaticFrameForEdit)

        staticFrameForEdit?.jsonPrimitive?.intOrNull?.let {

            if (it < 0) {
                map.remove(keyStaticFrameForEdit)

                val newValue: String = when (it) {
                    MediaVector.STATIC_FRAME_FOR_EDIT_LAST -> "last"
                    MediaVector.STATIC_FRAME_FOR_EDIT_MIDDLE -> "middle"
                    else -> throw IllegalStateException("unknown string static frame for edit ${it}")
                }
                map[keyStaticFrameForEdit] = JsonPrimitive(newValue)
            }
        }
    }
}


object MediaSerializer : JsonContentPolymorphicSerializer<Media>(Media::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<out Media> {
        val type = element.jsonObject["type"]?.jsonPrimitive?.content!!

        return when (type) {
            "image" -> MediaImageSerializer
            "group" -> MediaGroupSerializer
            "vector", "lottie" -> MediaVectorSerializer
            "path" -> MediaPathSerializer
            "text" -> MediaTextSerializer
            "textureMedia" -> MediaTextureSerializer
            else -> throw IllegalStateException("exception")
        }
    }

    override fun selectSerializer(value: Media): SerializationStrategy<Media> {
        return when (value) {
            is MediaText -> MediaTextSerializer
            is MediaGroup -> MediaGroupSerializer
            is MediaVector -> MediaVectorSerializer
            is MediaImage -> MediaImageSerializer
            is MediaPath -> MediaPathSerializer
            is MediaTexture -> MediaTextureSerializer
        } as SerializationStrategy<Media>
    }
}

object MediaTextSerializer : BaseMediaSerializer<MediaText>(MediaText.serializer()) {
    override fun transformDeserializeMap(map: MutableMap<String, JsonElement>) {
        super.transformDeserializeMap(map)
        val shadowOffset = map.remove("shadowOffset")?.jsonPrimitive?.floatOrNull
        if (shadowOffset != null) {
            map["shadowOffsetX"] = JsonPrimitive(shadowOffset)
            map["shadowOffsetY"] = JsonPrimitive(shadowOffset)
        }
    }
}

sealed class BaseMediaSerializer<T : Media>(serializer: KSerializer<T>) :
    JsonTransformingSerializer<T>(serializer) {

    open fun transformDeserializeMap(map: MutableMap<String, JsonElement>) {
        val layoutPositionMap = mutableMapOf<String, JsonElement>()
        val descriptor = LayoutPositionSerializer.descriptor

        for (i in 0 until descriptor.elementsCount) {
            val name = descriptor.getElementName(i)
            val el = map.remove(name)
            if (el != null) {
                layoutPositionMap[name] = el
            }
        }

        val isText = map["type"]?.jsonPrimitive?.content == "text"
        val isEditable = map["isEditable"]?.jsonPrimitive?.boolean ?: true
        val isSocial = map["isSocialIcon"]?.jsonPrimitive?.boolean ?: false
        val isMovable = map["isMovable"]?.jsonPrimitive?.boolean ?: false

        map["layoutPosition"] = JsonObject(layoutPositionMap)
        map["type"] = JsonPrimitive(descriptor.serialName)

        map.deserializeMillis("startTimeMillis", "startFrame")
        map.deserializeMillis("delayBeforeEndMillis", "delayBeforeEnd")
        map.deserializeMillis("loopedAnimationIntervalMillis", "loopedAnimationInterval")
        map.deserializeMillisMayString("minDurationMillis", "minDuration")

        val buttonsTAG = "touchActions"
        if (map[buttonsTAG] == null) {
            when {
                isSocial -> {
                    map[buttonsTAG] =
                        JsonArray(
                            listOf(
                                JsonPrimitive(TouchAction.button_scale.name),
                                JsonPrimitive(TouchAction.button_rotate.name)
                            )
                        )
                }
                isMovable || isText -> {
                    map[buttonsTAG] = JsonArray(
                        InspView.getDefaultMovableTouchActions().map { JsonPrimitive(it.name) }
                    )
                }
                isEditable -> {
                    map[buttonsTAG] =
                        JsonArray(
                            InspView.getDefaultEditableTouchActions().map { JsonPrimitive(it.name) }
                        )
                }
            }
        }
        val defaultSource = map["defaultSource"]?.jsonPrimitive?.content
        val originalSource = map["originalSource"]?.jsonPrimitive?.content ?: ""

        if (isSocial && defaultSource == null && originalSource.isNotEmpty())
            map["defaultSource"] = JsonPrimitive(originalSource)
    }

    open fun transformSerializeMap(map: MutableMap<String, JsonElement>) {
        map.unwrapNestedElement(LayoutPositionSerializer.descriptor.serialName)
        map["type"] = JsonPrimitive(descriptor.serialName)
    }

    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element is JsonObject) {

            val map = element.toMutableMap()

            transformDeserializeMap(map)

            return JsonObject(map)
        }
        return element
    }

    override fun transformSerialize(element: JsonElement): JsonElement {

        if (element is JsonObject) {

            val map = element.toMutableMap()

            transformSerializeMap(map)

            return JsonObject(map)
        }

        return element
    }
}

fun MutableMap<String, JsonElement>.unwrapNestedElement(nestedFieldName: String) {
    val nestedElement = remove(nestedFieldName)
    if (nestedElement != null && nestedElement is JsonObject) {
        nestedElement.forEach {
            this[it.key] = it.value
        }
    }
}

fun MutableMap<String, JsonElement>.deserializeMillis(keyMillis: String, keyFrames: String) {
    val startTimeMillis = this[keyMillis]?.jsonPrimitive?.doubleOrNull
    if (startTimeMillis != null) {
        this.remove(keyMillis)
        this[keyFrames] =
            JsonPrimitive((startTimeMillis / FRAME_IN_MILLIS).roundToInt())
    }
}

fun MutableMap<String, JsonElement>.deserializeMillisMayString(
    keyMillis: String,
    keyFrames: String
) {
    val minDurationMillis = this[keyMillis]?.jsonPrimitive
    if (minDurationMillis != null) {
        if (minDurationMillis.isString) {
            this[keyFrames] = JsonPrimitive(minDurationMillis.content)
            this.remove(keyMillis)
        } else
            deserializeMillis(keyMillis, keyFrames)
    }

    val startTimeMillis = this[keyMillis]?.jsonPrimitive?.doubleOrNull
    if (startTimeMillis != null) {
        this.remove(keyMillis)
        this[keyFrames] =
            JsonPrimitive((startTimeMillis / FRAME_IN_MILLIS).roundToInt())
    }
}