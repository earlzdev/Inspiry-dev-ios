package app.inspiry.core.serialization

import app.inspiry.core.animator.ANIMATOR_GROUP_ALL
import app.inspiry.core.animator.TextAnimationParams
import app.inspiry.core.data.FRAME_IN_MILLIS
import kotlinx.serialization.json.*

object TextAnimationParamsSerializer :
    JsonTransformingSerializer<TextAnimationParams>(TextAnimationParams.serializer()) {

    private fun MutableMap<String, JsonElement>.packAnimatorGroups(
        keyGroups: String,
        keyAnimators: String
    ) {
        val groups = this[keyGroups] ?: return

        if (groups.jsonArray.size == 1 && groups.jsonArray[0].jsonObject["group"]?.jsonPrimitive?.contentOrNull == ANIMATOR_GROUP_ALL) {
            val animatorsJson = this.remove(keyGroups)

            if (animatorsJson != null) {
                val animators = animatorsJson.jsonArray[0].jsonObject["animators"]
                if (animators != null)
                    this[keyAnimators] = animators
            }
        }
    }

    fun MutableMap<String, JsonElement>.unpackAnimators(
        animatorsKey: String,
        animatorGroupKey: String
    ) {
        val animators = remove(animatorsKey)
        if (animators != null) {
            val textGroupAnimators =
                JsonArray(
                    listOf(
                        JsonObject(
                            mapOf(
                                "group" to JsonPrimitive(ANIMATOR_GROUP_ALL),
                                "animators" to animators
                            )
                        )
                    )
                )
            this[animatorGroupKey] = textGroupAnimators
        }
    }

    fun MutableMap<String, JsonElement>.deserializeFramesToMillis(
        keyMillis: String,
        keyFrames: String
    ) {
        val frames = this[keyFrames]?.jsonPrimitive?.intOrNull
        if (frames != null) {
            this.remove(keyFrames)
            this[keyMillis] = JsonPrimitive(frames * FRAME_IN_MILLIS)
        }
    }

    override fun transformSerialize(element: JsonElement): JsonElement {
        if (element is JsonObject) {

            val map = element.toMutableMap()

            map.packAnimatorGroups("backgroundAnimatorGroups", "backgroundAnimators")
            map.packAnimatorGroups("textAnimatorGroups", "textAnimators")

            return JsonObject(map)
        }

        return element
    }

    override fun transformDeserialize(element: JsonElement): JsonElement {
        if (element is JsonObject) {

            val map = element.toMutableMap()

            map.deserializeFramesToMillis("charDelayMillis", "charDelay")
            map.deserializeFramesToMillis("wordDelayMillis", "wordDelay")
            map.deserializeFramesToMillis("lineDelayMillis", "lineDelay")

            map.unpackAnimators("textAnimators", "textAnimatorGroups")
            map.unpackAnimators("backgroundAnimators", "backgroundAnimatorGroups")

            return JsonObject(map)
        }

        return element
    }
}